/*
 * KenChu - Just a simple Robot for battle in Robocode.
 * Copyright (C) 2013 Nahuel Barrios <barrios.nahuel@gmail.com>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kenchu;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
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
public class KenChu extends Robot {

    public static final int ROBOT_SIZE = 36;

    private static final double DISTANCE_TO_RUN = ROBOT_SIZE * 4;

    private static final double WALL_PROXIMITY_CONSTANT = 0.2;

    private static final double DISTANCE_TO_GO_FOR_TARGET = ROBOT_SIZE * 4;

    private double battleFieldSizeAverage;

    public double initialEnergy;

    private double minimumEnergyToRam;

    @Override
    public void run() {
        initialEnergy = getEnergy();
        minimumEnergyToRam = initialEnergy / 2;

        final int degreesToRotateGun = 90;

        battleFieldSizeAverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;
        final double distanceToGoAhead = battleFieldSizeAverage / 5;
        final double distanceToGoBack = distanceToGoAhead / 2;

        setColors(Color.black, Color.black, Color.green);
        while (true) {
            ahead(distanceToGoAhead);
            scanForEnemies(degreesToRotateGun);
            back(distanceToGoBack);
            scanForEnemies(degreesToRotateGun);
        }
    }

    /**
     * TODO : Javadoc for scanForEnemies
     *
     * @param degreesToRotateGun
     */
    private void scanForEnemies(int degreesToRotateGun) {
        for (int degrees = 360; degrees != 0; degrees -= degreesToRotateGun) {
            turnGunRight(degreesToRotateGun);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        fire(calculateBestPowerForShoot(event.getDistance()));
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        turnLeft(90 - event.getBearing());

        if (isNearWall(getX(), getY())) {
            if (isHeadingWall()) {
                back(DISTANCE_TO_RUN);
            } else {
                ahead(DISTANCE_TO_RUN);
            }
        } else {
            ahead(DISTANCE_TO_RUN);
        }

        turnGunLeft(90 + event.getBearing());
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

    @Override
    public void onHitRobot(HitRobotEvent event) {
        double bearing = event.getBearing();

        if (event.isMyFault() && getEnergy() > minimumEnergyToRam) {
            turnRight(bearing);

            findEnemy(bearing);

            ahead(DISTANCE_TO_GO_FOR_TARGET);
        } else {
            if (bearing > -90 && bearing <= 90) {
                //  I'm heading him but I can't ram him.
                findEnemy(bearing);
                back(ROBOT_SIZE * 2);
            } else {
                //  It's not my fault and he's behind me.
                turnGunRight(getHeading() - 180 - getGunHeading());
                ahead(100);
            }
        }
    }

    private void findEnemy(double bearing) {
        double pos = getHeading() + bearing;
        if (bearing > 0) {
            //  It's on my right.
            if (getGunHeading() + pos > 360) {
                turnGunLeft(pos - getGunHeading());
            } else {
                turnGunRight(pos - getGunHeading());
            }
        } else {
            //  It's on my left.
            turnGunLeft(360);
        }
    }

    /**
     * Calculates which is the best power for fire to an enemy based on the distance that the enemy is.
     *
     * @param distance
     *         The distance to the enemy.
     *
     * @return 1, 2 or 3 depending on how close we are to the enemy.
     */
    private int calculateBestPowerForShoot(double distance) {
        int power = 1;

        if (distance < battleFieldSizeAverage / 2) {
            power = 2;
            if (distance < battleFieldSizeAverage / 3) {
                power = 3;
            }
        }

        return power;
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
     * TODO : Javadoc for isNearWall
     *
     * @param x
     * @param y
     *
     * @return
     */
    private boolean isNearWall(double x, double y) {
        boolean isNearWall = false;

        if (x < getBattleFieldWidth() * WALL_PROXIMITY_CONSTANT || x > getBattleFieldWidth() * (1 - WALL_PROXIMITY_CONSTANT)) {
            isNearWall = true;
            System.out.println("Is near left/right wall.");
        } else if (y < getBattleFieldHeight() * WALL_PROXIMITY_CONSTANT || y > getBattleFieldHeight() * (1 - WALL_PROXIMITY_CONSTANT)) {
            isNearWall = true;
            System.out.println("Is near bottom/top wall.");
        }

        return isNearWall;
    }

    private boolean isHeadingWall() {
//        double heading=getHeading();
//        if(heading)
        return false;
    }
}
