/*
 * TiroLioYCoshaGolda - Just a simple Robot for battle in Robocode.
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

package pnt;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * Main class for this robot. It has the logic needed to win lot of battles... xD
 * <p/>
 * Created on 8/7/13, at 7:28 PM.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 */
public class TiroLioYCoshaGolda extends Robot {

    public static final int ROBOT_SIZE = 36;

    private static final double DISTANCE_TO_RUN = ROBOT_SIZE * 4;

    private static final double DISTANCE_TO_RUN_BACK = DISTANCE_TO_RUN * 2;

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

        //noinspection InfiniteLoopStatement
        while (true) {
            ahead(distanceToGoAhead);
            scanForEnemies(degreesToRotateGun, true);
            back(distanceToGoBack);
            scanForEnemies(degreesToRotateGun, true);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double power = calculateBestPowerForShoot(event);

        if (power == 1 && getOthers() == 1) {
            power = 2;
        }

        fire(power);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        turnLeft(90 - event.getBearing());
        turnGunLeft(90 + event.getBearing());

        double x = getX();
        double y = getY();

        if (isNearWall(x, y) && isHeadingWall(x, y)) {
            back(DISTANCE_TO_RUN_BACK);
        } else {
            ahead(DISTANCE_TO_RUN);
        }
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

    /**
     * TODO : Javadoc for scanForEnemies
     *
     * @param degreesToRotateGun
     */
    private void scanForEnemies(int degreesToRotateGun, boolean turnLeft) {
        for (int degrees = 360; degrees != 0; degrees -= degreesToRotateGun) {
            if (turnLeft) {
                turnGunLeft(degreesToRotateGun);
            } else {
                turnGunRight(degreesToRotateGun);
            }
        }
    }

    /**
     * TODO : Javadoc for findEnemy
     *
     * @param bearing
     */
    private void findEnemy(double bearing) {
        //  TODO : Fix method findEnemy
        double pos = getHeading() + bearing;
        if (bearing > 0) {
            //  It's on my right.
            turnGunRight(360);
        } else {
            //  It's on my left.
            turnGunLeft(360);
        }
    }

    /**
     * Calculates which is the best power for fire to an enemy based on the distance that the enemy is.
     *
     * @param event
     *         The {@link ScannedRobotEvent}.
     *
     * @return 1, 2 or 3 depending on how close we are to the enemy.
     */
    private double calculateBestPowerForShoot(ScannedRobotEvent event) {
        double distance = event.getDistance();
        double power = 1;

        if (event.getVelocity() == 0 || distance <= battleFieldSizeAverage / 4) {
            power = Rules.MAX_BULLET_POWER;
        } else if (distance <= battleFieldSizeAverage / 3) {
            power = 2.5;
        } else if (distance <= battleFieldSizeAverage / 2) {
            power = 2;
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
     * Check whether we are near a wall by standing in front of it or if the wall is behind us.
     *
     * @param x
     *         The X coordinate.
     * @param y
     *         The Y coordinate.
     *
     * @return {@code true} when we are near a wall. Otherwise {@code false}.
     */
    private boolean isNearWall(double x, double y) {
        boolean isNearWall = false;

        if (x < getBattleFieldWidth() * WALL_PROXIMITY_CONSTANT || x > getBattleFieldWidth() * (1 - WALL_PROXIMITY_CONSTANT)) {
            isNearWall = true;
        } else if (y < getBattleFieldHeight() * WALL_PROXIMITY_CONSTANT || y > getBattleFieldHeight() * (1 - WALL_PROXIMITY_CONSTANT)) {
            isNearWall = true;
        }

        return isNearWall;
    }

    /**
     * Checks if we are heading a wall based on the position coordinates.
     *
     * @param x
     *         The X coordinate.
     * @param y
     *         The Y coordinate.
     *
     * @return {@code true} if we are heading a wall or {@code false} when the wall is behind us.
     */
    private boolean isHeadingWall(double x, double y) {
        boolean isHeadingWall = false;
        double heading = getHeading();

        if (heading >= 0 && heading < 90) {
            if (x > getBattleFieldWidth() * (1 - WALL_PROXIMITY_CONSTANT) || y > getBattleFieldHeight() * (1 - WALL_PROXIMITY_CONSTANT)) {
                isHeadingWall = true;
            }
        } else if (heading >= 90 && heading < 180) {
            if (x > getBattleFieldWidth() * (1 - WALL_PROXIMITY_CONSTANT) || y < getBattleFieldHeight() * WALL_PROXIMITY_CONSTANT) {
                isHeadingWall = true;
            }
        } else if (heading >= 180 && heading < 270) {
            if (x < getBattleFieldWidth() * WALL_PROXIMITY_CONSTANT || y < getBattleFieldHeight() * WALL_PROXIMITY_CONSTANT) {
                isHeadingWall = true;
            }
        } else {
            //  270-360
            if (x < getBattleFieldWidth() * WALL_PROXIMITY_CONSTANT || y > getBattleFieldHeight() * (1 - WALL_PROXIMITY_CONSTANT)) {
                isHeadingWall = true;
            }
        }

        return isHeadingWall;
    }
}
