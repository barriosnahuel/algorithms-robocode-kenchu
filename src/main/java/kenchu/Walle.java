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
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        fire(1);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        back(10);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        final double degreesToGoOut = 90;

        double bearing = event.getBearing();

        boolean turnRight = true;
        if (bearing > 0) {
            turnRight(bearing + degreesToGoOut);
        } else if (bearing != -180) {
            turnLeft(bearing * -1 + degreesToGoOut);
            turnRight = false;
        }

        ahead(ROBOT_SIZE);

        if (turnRight) {
            turnRight(degreesToGoOut);
        } else {
            turnLeft(degreesToGoOut);
        }
    }
}
