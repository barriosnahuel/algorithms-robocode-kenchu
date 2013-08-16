package pnt;

import robocode.BulletHitEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * TODO : Javadoc for
 * <p/>
 * Created on 16/08/13, at 12:39.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 */
public class DynamicTarget extends BaseRobot {

    private String targetName;

    @Override
    public void run() {
        super.run();

        setColors(Color.black, Color.black, Color.white);

        while (true) {
            moveForDynamicTargetMode();
        }
    }

    private void moveForDynamicTargetMode() {
        turnRadarRight(360);
        turnRadarLeft(90);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {

        String enemyName = event.getName();
        if (targetName == null || targetName.equals(enemyName)) {
            targetName = enemyName;

            double heading = getHeading();
            double bearing = event.getBearing();
            double absolute = heading + bearing;

            boolean rescan = false;

            if (getGunHeat() > 0) {
                rescan = true;
                turnRight(bearing);
            }

            while (getGunHeat() > 0) {
                rescan = true;
                ahead(event.getDistance() / 2);
            }

            turnGun(absolute, getGunHeading());
            turnRadar(absolute, getRadarHeading());
            if (rescan) {
                scan();
            } else {
                handleFire(event);
            }
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        if (event.getName().equals(targetName)) {
            if (event.getEnergy() == 0) {
                cleanTargetInformation();
            }
        }
    }

    private void cleanTargetInformation() {
        targetName = null;
    }

    @Override
    protected double calculateBestPowerForShooting(ScannedRobotEvent event) {
        double distance = event.getDistance();
        double myEnergy = getEnergy();
        double power = 1;

        if (myEnergy < minimumEnergyToStayAlive) {
            power = Rules.MIN_BULLET_POWER;
        } else if (distance <= battleFieldSizeAverage / 6) {
            power = Rules.MAX_BULLET_POWER;
        } else if (distance <= battleFieldSizeAverage / 5) {
            power = 2.5;
        } else if (distance <= battleFieldSizeAverage / 4) {
            power = 2;
        } else if (distance <= battleFieldSizeAverage / 3) {
            power = 1.5;
        }

        if (myEnergy < minimumEnergyToFireBigBullets) {
            power -= 0.5;
        }

        return power;
    }
}
