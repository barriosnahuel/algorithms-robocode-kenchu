package kenchu;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * Main class for this robot. It has the logic needed to win lot of battles... xD
 * <p/>
 * Created on 8/7/13, at 7:28 PM.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 */
public class Walle extends Robot {

    public static final int ROBOT_SIZE = 36;

    @Override
    public void run() {

        setColors(Color.black, Color.black, Color.green);

        while (true) {
            ahead(50);
            turnGunRight(360);
            back(50);
            turnGunLeft(360);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double distance = event.getDistance();

        int power = calculateBestPowerForShoot(distance);

        fire(power);

        if (power > 1) {
            scan();
        }

        ahead(ROBOT_SIZE);

        double bearing = event.getBearing();
        if (bearing > 0) {
            turnRight(bearing);
        } else {
            turnLeft(bearing);
        }

        ahead(distance / 2);
        scan();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        turnLeft(90 - event.getBearing());
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        final double degreesToGoOut = 90;

        boolean turnRight = changeDirection(event.getBearing(), degreesToGoOut);

        ahead(ROBOT_SIZE);

        if (turnRight) {
            turnRight(degreesToGoOut);
        } else {
            turnLeft(degreesToGoOut);
        }
    }

    /**
     * TODO : Javadoc for changeDirection
     *
     * @param bearing
     * @param degrees
     *
     * @return
     */
    private boolean changeDirection(double bearing, double degrees) {
        boolean changedRight = true;

        if (bearing > 0) {
            turnRight(bearing + degrees);
        } else if (bearing != -180) {
            turnLeft(bearing * -1 + degrees);
            changedRight = false;
        }

        return changedRight;
    }

    /**
     * TODO : Javadoc for calculateBestPowerForShoot
     *
     * @param distance
     *
     * @return
     */
    private int calculateBestPowerForShoot(double distance) {
        int power = 1;

        double battleFieldSizeaverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;

        if (distance < battleFieldSizeaverage / 2) {
            power = 2;
            if (distance < battleFieldSizeaverage / 3) {
                power = 3;
            }
        }

        return power;
    }
}
