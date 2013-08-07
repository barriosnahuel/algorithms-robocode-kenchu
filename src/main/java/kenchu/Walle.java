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

    /**
     * run: Walle's default behavior
     */
    public void run() {
        // Initialization of the robot should be put here

        // After trying out your robot, try uncommenting the import at the top,
        // and the next line:

        setColors(Color.black, Color.black, Color.green); // body,gun,radar

        // Robot main loop
        while (true) {
            ahead(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);
        }
    }

    /**
     * onScannedRobot: What to do when you see another robot
     */
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        // Replace the next line with any behavior you would like
        fire(1);
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        // Replace the next line with any behavior you would like
        back(10);
    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    @Override
    public void onHitWall(HitWallEvent event) {
        // Replace the next line with any behavior you would like
        back(20);
    }
}
