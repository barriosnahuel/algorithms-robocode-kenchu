package pnt;

import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * TODO : Javadoc for DynamicTarget
 * <p/>
 * Created on 16/08/13, at 12:39.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 */
public class DynamicTarget extends BaseRobot {

    private String targetName;

    private double targetEnergy;

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
        double enemyEnergy = event.getEnergy();
        if (targetName == null || targetName.equals(enemyName) || enemyEnergy < targetEnergy) {
            targetName = enemyName;
            targetEnergy = enemyEnergy;

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

                double distance = event.getDistance();
                if (distance < ROBOT_SIZE) {
                    distance = ROBOT_SIZE;
                } else {
                    distance = distance / 2;
                }
                ahead(distance);
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
        checkForTargetAndCleanInformation(event.getName(), event.getEnergy());
        //  TODO : Functionality : If is other enemy and it has less energy than target, change target!
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        checkForTargetAndCleanInformation(event.getName(), event.getEnergy());
        //  TODO : Functionality : If is other enemy and it has less energy than target, change target!
    }

    private void checkForTargetAndCleanInformation(String enemyName, double enemyEnergy) {
        if (enemyName.equals(targetName)) {
            targetEnergy = enemyEnergy;
            if (targetEnergy == 0) {
                cleanTargetInformation();
            }
        }
    }

    private void cleanTargetInformation() {
        targetName = null;
        targetEnergy = -1;
    }
}
