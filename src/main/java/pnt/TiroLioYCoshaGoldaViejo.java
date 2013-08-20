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

import robocode.BulletHitEvent;
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
public class TiroLioYCoshaGoldaViejo extends Robot {

    public static final int ROBOT_SIZE = 36;

    private static final double DISTANCE_TO_RUN = ROBOT_SIZE * 4;


    private static final double LIMIT_TO_CONSIDER_STATIC_TARGET = 1.5;

    private static final double DISTANCE_TO_GO_FOR_TARGET = ROBOT_SIZE * 4;

    private static final double WALL_PROXIMITY_CONSTANT = 0.2;

    private double battleFieldSizeAverage;

    public double initialEnergy;


    private double minimumEnergyToFireBigBullets;

    private double minimumEnergyToFireSmallestBullets;

    private double minimumEnergyToStayAlive;


    private int notFiredBullets = 0;

    private double distanceToMoveWhenOneOnOne;

    //    ************************************  ENUMS
    //    ***********
    //    How to set the body respect another object: wall/enemy/bullet
    private static final int BODY_HEADING = 1;

    private static final int BODY_PERPENDICULAR = 2;

    //    ***********
    //    Attack modes
    private static final int ATTACK_MODE_STAY_ALIVE = 0;

    private static final int ATTACK_MODE_EVERYBODY_AGAINST_EVERYBODY = 1;

    private static final int ATTACK_MODE_STATIC_TARGET = 2;

    private static final int ATTACK_MODE_DYNAMIC_TARGET = 3;

    private static final int ATTACK_MODE_ONE_ON_ONE = 4;

    //    ************************************  ENUMS

    private int attackMode = ATTACK_MODE_EVERYBODY_AGAINST_EVERYBODY;

    private int previousAttackMode;

    private boolean isAttackModeSet;

    private double minimumEnergyToRam;

    private double minimumEnergyToContinueFiringWhenHittedByBullet;

    private double minimumEnergyToChase;

    private int firedBullets;

    private double targetEnergy;

    private int isTargetBulletCounter;

    private static final int IS_TARGET_BULLET_COUNTER_LIMIT = 1;//  TODO : Update this 1 to 2 or 3!!

    private String isTargetEnemyName;

    private double isTargetEnemyAbsoluteAngle;

    @Override
    public void run() {
        initialEnergy = getEnergy();
        minimumEnergyToRam = initialEnergy / 2;

        minimumEnergyToContinueFiringWhenHittedByBullet = initialEnergy * 0.35;

//         PROPERTIES
        battleFieldSizeAverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;
        distanceToMoveWhenOneOnOne = battleFieldSizeAverage / 5;

        minimumEnergyToFireBigBullets = initialEnergy * 0.3;
        minimumEnergyToFireSmallestBullets = initialEnergy * 0.15;
        minimumEnergyToStayAlive = initialEnergy * 0.08;
        minimumEnergyToChase = initialEnergy * 0.45;
//        /PROPERTIES

        setColors(Color.black, Color.black, Color.green);
        setBulletColor(Color.cyan);
        setScanColor(Color.green);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        //noinspection InfiniteLoopStatement
        while (true) {
            setAttackMode();

            switch (attackMode) {
                case ATTACK_MODE_STAY_ALIVE:
                    moveForStayingAlive();
                    break;
                case ATTACK_MODE_STATIC_TARGET:
                    moveForStaticTargetMode();
                    break;
                case ATTACK_MODE_DYNAMIC_TARGET:
                    moveForDynamicTargetMode();
                    break;
                case ATTACK_MODE_ONE_ON_ONE:
                    moveForOneOnOneMode();
                    break;
                default:
                    moveForEverybodyAgainstEverybody();
            }
        }
    }

    private void moveForStayingAlive() {
        ahead(ROBOT_SIZE);
        back(ROBOT_SIZE);
    }

    private void moveForStaticTargetMode() {
        turnRadarRight(15);
        turnRadarLeft(30);
        turnRadarRight(15);
        ahead(ROBOT_SIZE * 3);

        turnRadarRight(15);
        turnRadarLeft(30);
        turnRadarRight(15);
        ahead(ROBOT_SIZE * 3);

        restoreAttackMode();
    }

    private void moveForDynamicTargetMode() {
        turnRadarRight(45);
        turnRadarLeft(90);
        turnRadarRight(360);
    }

    private void moveForOneOnOneMode() {
        turnRadarRight(360);
        ahead(distanceToMoveWhenOneOnOne);
        turnRadarRight(360);
        back(distanceToMoveWhenOneOnOne);
    }

    private void moveForEverybodyAgainstEverybody() {
        scanForEnemies(4, false);
        ahead(ROBOT_SIZE * 4);
        scanForEnemies(4, false);
        back(ROBOT_SIZE * 4);
    }

    private void setAttackMode() {
        if (getEnergy() <= minimumEnergyToStayAlive) {
            attackMode = ATTACK_MODE_STAY_ALIVE;
        } else {
            if (getOthers() > 1) {
                attackMode = ATTACK_MODE_EVERYBODY_AGAINST_EVERYBODY;
            } else if (!isAttackModeSet) {
                attackMode = ATTACK_MODE_ONE_ON_ONE;
                isAttackModeSet = true;
            }
        }
    }

    private void setAttackMode(ScannedRobotEvent event) {
        setAttackMode();

        if (event.getVelocity() <= LIMIT_TO_CONSIDER_STATIC_TARGET) {
            previousAttackMode = attackMode;
            attackMode = ATTACK_MODE_STATIC_TARGET;
            isAttackModeSet = true;
        }

        if (event.getEnergy() <= minimumEnergyToChase && getEnergy() >= event.getEnergy()) {
            previousAttackMode = attackMode;
            attackMode = ATTACK_MODE_DYNAMIC_TARGET;
            isAttackModeSet = true;
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        setAttackMode(event);

        switch (attackMode) {
            case ATTACK_MODE_STAY_ALIVE:
                onScannedRobotForStayingAlive(event);
                break;
            case ATTACK_MODE_STATIC_TARGET:
                onScannedRobotForStaticTarget(event);
                break;
            case ATTACK_MODE_DYNAMIC_TARGET:
                onScannedRobotForDynamicTarget(event);
                break;
            case ATTACK_MODE_ONE_ON_ONE:
                onScannedRobotForOneOnOne(event);
                break;
            default:
                onScannedRobotForEverybodyAgainstEverybody(event);
        }
    }

    private void onScannedRobotForStayingAlive(ScannedRobotEvent event) {
        if (event.getEnergy() < getEnergy()) {
            handleFire(event);
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        double bearing = event.getBearing();
        double angle = getHeading() + bearing;

        if (event.isMyFault() && getEnergy() > minimumEnergyToRam) {
            turnRight(bearing);
            turnGun(angle);
            handleFire();
            turnRadar(angle);

            //  TODO : Functionality : Set attack mode "Ramming" to fire with 3 points of energy or something special.

            ahead(DISTANCE_TO_GO_FOR_TARGET);
        } else {
            turnRadar(angle);

            if (bearing > -90 && bearing <= 90) {
                //  I'm heading him but I can't ram him.
                back(ROBOT_SIZE * 2);
            } else {
                //  It's not my fault and he's behind me.
                ahead(ROBOT_SIZE * 4);
            }
        }
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        turnRadarRight(360);

        if (isHeadingWall(getX(), getY())) {
            back(ROBOT_SIZE * 2);
        } else {
            ahead(ROBOT_SIZE * 2);
        }

        rotateBody(BODY_PERPENDICULAR, event.getBearing());
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {

        switch (attackMode) {
            case ATTACK_MODE_STAY_ALIVE:
                setAttackMode();
                break;
            case ATTACK_MODE_STATIC_TARGET:
                targetEnergy = event.getEnergy();
                break;
            case ATTACK_MODE_DYNAMIC_TARGET:
                targetEnergy = event.getEnergy();
                if (event.getEnergy() == 0) {
                    stop();
                    restoreAttackMode();
                }
            default:
        }
    }

    private void handleFire() {
        if (fireBullet(Rules.MAX_BULLET_POWER) != null) {
            firedBullets++;
        } else {
            System.out.println("Can't fire! (time n°: " + ++notFiredBullets + ")");
        }
    }

    private void onScannedRobotForOneOnOne(ScannedRobotEvent event) {
        if (getGunHeat() > 0) {
            turnRadarLeft(15);
            turnRadarRight(30);
            turnRadarLeft(15);
        }

        double firstHeading = getHeading();
        double bearing = event.getBearing();

        if (Math.abs(bearing) != 90) {
            rotateBody(BODY_PERPENDICULAR, bearing);
        }

        turnGun(firstHeading + bearing);
        turnRadar(firstHeading + bearing);
        handleFire(event);
    }

    private void onScannedRobotForDynamicTarget(ScannedRobotEvent event) {
        double originalBearing = event.getBearing();
        double firstHeading = getHeading();
        turnGun(firstHeading + originalBearing);

        boolean shoot = true;
        boolean rotateBody = true;

        if (getGunHeat() > 0) {
            rotateBody(BODY_HEADING, originalBearing);
            rotateBody = false;
            shoot = false;
        }

        if (getGunHeat() > 0) {
            turnRadar(firstHeading + originalBearing);
        }

        while (getGunHeat() > 0) {
            ahead(event.getDistance() / 4);
            shoot = false;
        }

        if (shoot) {
            handleFire(event);
        } else {
            ahead(event.getDistance() / 4);
        }

        if (rotateBody) {
            rotateBody(BODY_HEADING, originalBearing);
            ahead(event.getDistance() / 4);
        }

        moveForDynamicTargetMode();
    }

    private void onScannedRobotForEverybodyAgainstEverybody(ScannedRobotEvent event) {
        double firstHeading = getHeading();
        turnGun(firstHeading + event.getBearing());
        handleFire(event);
        turnRadar(firstHeading + event.getBearing());
    }


    private void onScannedRobotForStaticTarget(ScannedRobotEvent event) {
        double bearing = event.getBearing();

        double firstHeading = getHeading();

        turnRadar(firstHeading + bearing);
        turnGun(firstHeading + bearing);

        if (getGunHeat() > 0) {
            rotateBody(BODY_HEADING, event.getBearing());
        }

        scanForEnemiesAfterWaitingForGunHeat();

        handleFire(event);
    }

    private void scanForEnemiesAfterWaitingForGunHeat() {
        setAdjustRadarForGunTurn(false);
        turnGunRight(10);
        turnGunLeft(20);
        turnGunRight(10);
        setAdjustRadarForGunTurn(true);
    }

    private void restoreAttackMode() {
        attackMode = previousAttackMode;
        previousAttackMode = -1;
        if (attackMode != ATTACK_MODE_ONE_ON_ONE) {
            isAttackModeSet = false;
        }
    }

    /**
     * Calculates which is the best power for fire to an enemy based on the distance that the enemy is.
     *
     * @param event
     *         The {@link robocode.ScannedRobotEvent}.
     *
     * @return 1, 2 or 3 depending on how close we are to the enemy.
     */
    private double calculateBestPowerForShooting(ScannedRobotEvent event) {
        double distance = event.getDistance();
        double power = 1;

        switch (attackMode) {
            case ATTACK_MODE_STAY_ALIVE:
                power = Rules.MIN_BULLET_POWER;
                break;
            case ATTACK_MODE_STATIC_TARGET:
                if (distance <= battleFieldSizeAverage / 5) {
                    power = Rules.MAX_BULLET_POWER;
                } else if (distance <= battleFieldSizeAverage / 4) {
                    power = 2.5;
                } else {
                    power = 2;
                }
                break;
            case ATTACK_MODE_ONE_ON_ONE:
                if (distance <= battleFieldSizeAverage / 5) {
                    power = Rules.MAX_BULLET_POWER;
                } else if (distance <= battleFieldSizeAverage / 4) {
                    power = 2.5;
                } else if (distance <= battleFieldSizeAverage / 4) {
                    power = 2;
                } else if (distance <= battleFieldSizeAverage / 3) {
                    power = 1;
                } else {
                    power = 0.5;
                }
                break;
            default:
                if (distance <= battleFieldSizeAverage / 6) {
                    power = Rules.MAX_BULLET_POWER;
                } else if (getEnergy() < minimumEnergyToStayAlive) {
                    power = Rules.MIN_BULLET_POWER;
                } else if (getEnergy() < minimumEnergyToFireSmallestBullets) {
                    power = 0.5;
                } else if (getEnergy() < minimumEnergyToFireBigBullets) {
                    power = 1;
                } else if (distance <= battleFieldSizeAverage / 5) {
                    power = 2.5;
                } else if (distance <= battleFieldSizeAverage / 4) {
                    power = 2;
                }

                if (power == 1 && getOthers() == 1) {
                    power = 2;
                }
        }

        return power;
    }

    /**
     * Handles the fire action to get statistics about fired bullets.
     *
     * @param event
     */
    private void handleFire(ScannedRobotEvent event) {
        if (fireBullet(calculateBestPowerForShooting(event)) != null) {
            firedBullets++;
        } else {
            System.out.println("Can't fire! (time n°: " + ++notFiredBullets + ")");
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        setAttackMode();
    }

    /**
     * Rotates the body of the robot to stay horizontally to the specified robot/wall/bullet origin.
     *
     * @param mode
     *         The final desired state of the body against the robot/wall/bullet: BODY_HEADING or BODY_PERPENDICULAR.
     * @param bearing
     *         The bearing to the robot/wall/bullet origin, in degrees.
     */
    private void rotateBody(int mode, double bearing) {
        if (mode == BODY_PERPENDICULAR) {
            if (bearing > 0) {
                turnLeft(90 - bearing);
            } else {
                turnRight(90 - Math.abs(bearing));
            }
        } else {
            turnRight(bearing);
        }
    }

    private void turnRadar(double absolute) {
        double angle = absolute - getRadarHeading();

        if (absolute > 0) {
            if (angle > 180) {
                turnRadarLeft(360 - absolute + getRadarHeading());
            } else if (angle > 0) {
                //  y menor a 180, por la condicion anterior
                turnRadarRight(angle);
            } else if (angle < 0 && angle > -180) {
                turnRadarRight(angle);//  da negativo y va para la izquierda
            } else if (angle < -180) {
                turnRadarLeft(360 - getRadarHeading() + absolute);
            }
        } else {
            turnRadarRight(absolute);
        }
    }

    private void turnGun(double absolute) {
        double gunHeading = getGunHeading();
        double angle = absolute - gunHeading;

        if (absolute > 0) {
            if (angle > 180) {
                turnGunLeft(360 - absolute + gunHeading);
            } else if (angle > 0) {
                //  y menor a 180, por la condicion anterior
                turnGunRight(angle);
            } else if (angle < 0 && angle > -180) {
                turnGunRight(angle);//  da negativo y va para la izquierda
            } else if (angle < -180) {
                turnGunLeft(360 - gunHeading + absolute);
            }
        } else {
            turnGunRight(absolute);
        }
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

    private void scanForEnemies(int steps, boolean turnLeft) {
        double degrees = 360 / steps;
        for (double left = 360; left > 0; left -= degrees) {
            if (turnLeft) {
                turnRadarLeft(degrees);
            } else {
                turnRadarLeft(degrees);
            }
        }
    }
}
