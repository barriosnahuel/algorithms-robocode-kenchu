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

import robocode.*;
import robocode.Robot;

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

    private double minimumEnergyToFireBigBullets;

    private double minimumEnergyToFireSmallestBullets;

    private boolean isEscaping;

    private int firedBullets = 0;

    private int hittedBullets = 0;

    private int missedFiredBullets = 0;

    private int notFiredBullets = 0;

    private double distanceToMoveWhenOneOnOne;

    private AttackModes attackMode = AttackModes.EVERYBODY_AGAINST_EVERYBODY;

    private boolean isAttackModeSet;

    private AttackModes previousAttackMode;

    @Override
    public void run() {
        initialEnergy = getEnergy();
        minimumEnergyToRam = initialEnergy / 2;
        minimumEnergyToFireBigBullets = initialEnergy * 0.3;
        minimumEnergyToFireSmallestBullets = initialEnergy * 0.1;

        battleFieldSizeAverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;
        final double distanceToGoAhead = battleFieldSizeAverage / 5;
        final double distanceToGoBack = distanceToGoAhead / 2;
        distanceToMoveWhenOneOnOne = battleFieldSizeAverage / 3;

        setColors(Color.black, Color.black, Color.green);
        setBulletColor(Color.cyan);

        //noinspection InfiniteLoopStatement
        while (true) {
            updateAttackModeWhenCorresponding();

            if (attackMode == AttackModes.ONE_ON_ONE) {
                battleOneOnOne();
            } else {
                battleEverybodyAgainstEverybody(distanceToGoAhead, distanceToGoBack);
            }
        }
    }

    /**
     * Runs the main strategy when battling against only one enemy.
     */
    private void battleOneOnOne() {
        turnRadarRight(360);
        ahead(distanceToMoveWhenOneOnOne);
        turnRadarRight(360);
        back(distanceToMoveWhenOneOnOne);
    }

    /**
     * Runs the main strategy when battling against more than one enemy.
     *
     * @param distanceToGoAhead
     *         Distance to move ahead.
     * @param distanceToGoBack
     *         Distance to move back
     */
    private void battleEverybodyAgainstEverybody(double distanceToGoAhead, double distanceToGoBack) {
        handleMovement(Direction.AHEAD, distanceToGoAhead);
        scanForEnemies(40, true);
        handleMovement(Direction.BACK, distanceToGoBack);
        scanForEnemies(40, true);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        System.out.println("Scanned robot: " + event.getName() + " (" + event.getEnergy() + ")");

        //  This is in case there are 2 enemies left and one kills the other. Then I see the 1 left but not from the main loop, so...
        updateAttackModeWhenCorresponding();

        boolean enemyIsNotMoving = event.getVelocity() < 2;

        if (enemyIsNotMoving) {
            previousAttackMode = attackMode;
            attackMode = AttackModes.TARGETING;
        }

        if (attackMode == AttackModes.ONE_ON_ONE) {
            if (getGunHeat() > 0) {
                turnGunLeft(5);
                turnGunRight(10);
                turnGunLeft(5);
            }

            attack(event);
        } else {
            if (getGunHeat() == 0 || attackMode == AttackModes.TARGETING) {
                boolean attack = true;

                if (isEscaping) {
                    //  TODO : Functionality : If I can kill enemy with just one or two bullets, then kill him.
                    if ((getEnergy() < initialEnergy * 0.3 || event.getEnergy() > initialEnergy * 0.2) && attackMode != AttackModes.ONE_ON_ONE) {
                        System.out.println("I'm escaping from an enemy, then I won't stay quite to attack another one.");
                        attack = false;
                    }
                }

                if (attack) {
                    attack(event);
                }
            } else {
                System.out.println("Gun is " + getGunHeat() + " heat: skipping trying to fire.");
            }
        }
    }

    /**
     * Sets the {@link #attackMode} to ONE_ON_ONE if there is only one enemy left and the {@link #isAttackModeSet} field is not set yet.
     */
    private void updateAttackModeWhenCorresponding() {
        if (!isAttackModeSet && getOthers() == 1) {
            attackMode = AttackModes.ONE_ON_ONE;
            isAttackModeSet = true;
        }
    }

    /**
     * Fire. Finally this method will execute the call to {@link #fire(double) for each {@link AttackModes}.
     *
     * @param event
     *         The {@link ScannedRobotEvent}.
     */
    private void attack(ScannedRobotEvent event) {
        //  TODO : Functionality : Improve bullet power calculation when one on one or targeting.

        switch (attackMode) {
            case TARGETING:
                handleTargeting(event.getBearing());
                break;
            case ONE_ON_ONE:
                handleOneOnOneAttackStrategy(event);
                break;
            default:
                handleFire(calculateBestPowerForShooting(event));
        }
    }

    /**
     * Reorganize radar to the same angle that the gun is and perform a fire/scan action.
     * <p/>
     * This method should be called only after first {@link ScannedRobotEvent} in one on one battles.
     *
     * @param event
     *         The The {@link ScannedRobotEvent}.
     */
    private void handleOneOnOneAttackStrategy(ScannedRobotEvent event) {
        turnRadarToGun(event.getBearing());

        handleFire(calculateBestPowerForShooting(event));
    }

    /**
     * Turn radar left/right to leave it at the same angle that the gun is.
     *
     * @param bearing
     *         The bearing to the enemy.
     */
    private void turnRadarToGun(double bearing) {
        stop(true);

        if (Math.abs(bearing) != 90) {
            setAdjustGunForRobotTurn(true);
            rotateToHorizontallyAgainst(bearing);
            setAdjustGunForRobotTurn(false);
        }

        setAdjustRadarForGunTurn(true);
        if (getHeading() == getGunHeading()) {
            turnGunRight(bearing);
        } else {
            turnGunRight(getHeading() - getGunHeading() + bearing);
        }
        setAdjustRadarForGunTurn(false);

        if (bearing > 0) {
            turnRadarLeft(getRadarHeading() - getGunHeading());
        } else {
            turnRadarRight(getGunHeading() - getRadarHeading());
        }
    }

    /**
     * Rotates the body of the robot to stay horizontally to the specified robot/wall/bullet origin.
     *
     * @param bearing
     *         The bearing to the robot/wall/bullet origin, in degrees.
     */
    private void rotateToHorizontallyAgainst(double bearing) {
        if (bearing > 0) {
            turnLeft(90 - bearing);
        } else {
            turnRight(90 - Math.abs(bearing));
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
        //  TODO : Performance : Check if this method can be removed and use its code into rotateToHorizontallyAgainst
        boolean changedRight = true;

        if (bearing > 0) {
            turnRight(bearing + degrees);
        } else if (bearing != -180) {
            turnLeft(bearing * -1 + degrees);
            changedRight = false;
        }

        return changedRight;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        isEscaping = true;
        if (attackMode == AttackModes.TARGETING) {
            attackMode = previousAttackMode;
        }

        //  TODO : Functionality : If bullet is from the one that is my target, then evaluate continue shooting.
        turnLeft(90 - event.getBearing());

        double x = getX();
        double y = getY();

        if (isNearWall(x, y) && isHeadingWall(x, y)) {
            back(getDistanceToRun(Direction.BACK));
        } else {
            ahead(getDistanceToRun(Direction.AHEAD));
        }

        isEscaping = false;

        //  TODO : Fix this..
        turnGunLeft(90 + event.getBearing());
    }

    /**
     * Calculates the best distance to move the robot for the specified {@code directionToRun} and taking into account the current {@link
     * #attackMode}.
     *
     * @param directionToRun
     *         Direction indicating where to move.
     *
     * @return The distance to move.
     */
    private double getDistanceToRun(Direction directionToRun) {
        //  Default is for run ahead.
        double distance = DISTANCE_TO_RUN;

        if (attackMode == AttackModes.ONE_ON_ONE) {
            distance = battleFieldSizeAverage * 0.6;
        } else if (directionToRun == Direction.BACK) {
            distance = DISTANCE_TO_RUN_BACK;
        }

        return distance;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        rotateToHorizontallyAgainst(event.getBearing());

        //  TODO : Functionality : Search for enemies after hitting a wall!
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        double bearing = event.getBearing();

        if (event.isMyFault() && getEnergy() > minimumEnergyToRam) {
            turnRight(bearing);

//            Call with 0 because I've already moved the gun when turning right the entire robot.
            //  TODO : Refactor :  This call with 0 is horrible because I know the implementation of findEnemy(x)
            findEnemy(0);

            ahead(DISTANCE_TO_GO_FOR_TARGET);
        } else {
            findEnemy(bearing);

            if (bearing > -90 && bearing <= 90) {
                //  I'm heading him but I can't ram him.
                back(ROBOT_SIZE * 2);
            } else {
                //  It's not my fault and he's behind me.
                ahead(100);
            }
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        System.out.println("Bullet hitted " + event.getName() + "! (time n°: " + ++hittedBullets + ")");
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        System.out.println("Bullet missed! (time n°: " + ++missedFiredBullets + ")");
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        System.out.println("Bullet hit bullet (missed) (time n°: " + ++missedFiredBullets + ")");
    }

    @Override
    public void onDeath(DeathEvent event) {
        System.out.println("Fired bullets: " + firedBullets);
        System.out.println("Hitted bullets: " + hittedBullets);
        System.out.println("Missed bullets: " + missedFiredBullets);
        System.out.println("Not fired bullets: " + notFiredBullets);
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        System.out.println("Fired bullets: " + firedBullets);
        System.out.println("Hitted bullets: " + hittedBullets);
        System.out.println("Missed fired bullets: " + missedFiredBullets);
        System.out.println("Not fired bullets: " + notFiredBullets);
    }

    /**
     * Handles the fire action to get statistics about fired bullets.
     *
     * @param power
     *         The power with which to shoot
     */
    private void handleFire(double power) {
        if (fireBullet(power) != null) {
            firedBullets++;
        } else {
            System.out.println("Can't fire! (time n°: " + ++notFiredBullets + ")");
        }
    }

    /**
     * Attacks an enemy that is not moving anyway by stopping all other actions and scanning him again and again.
     * <p/>
     * <b>Important: </b>If we see a static enemy while we're escaping after receive a bullet from any other enemy, then we fire and continues
     * escaping.
     *
     * @param bearing
     *         The bearing to the enemy.
     */
    private void handleTargeting(double bearing) {
        System.out.println("Attacking a static enemy.");

        if (previousAttackMode == AttackModes.ONE_ON_ONE) {
            turnRadarToGun(bearing);
        }

        handleFire(Rules.MAX_BULLET_POWER);

        stop(true);

        if (isEscaping) {
            System.out.println("I'm escaping from an enemy, then I won't stay quite to attack another one.");
            resume();
        } else {
            attackMode = previousAttackMode;
            scan();
        }
    }

    /**
     * Turn the gun left/right based on {@code turnLeft} parameter in steps based on {@code degreesToRotateGun} till achieve 180 rotated degrees.
     *
     * @param degreesToRotateGun
     *         Degrees to rotate in each step till achieve 180 rotated degrees.
     */
    private void scanForEnemies(int degreesToRotateGun, boolean turnLeft) {
        for (int degrees = 180; degrees > 0; degrees -= degreesToRotateGun) {
            if (turnLeft) {
                turnGunLeft(degreesToRotateGun);
            } else {
                turnGunRight(degreesToRotateGun);
            }
        }
    }

    /**
     * Find the enemy based on the bearing that we've got to him. It will be evaluated by turning the gun to the right.
     *
     * @param bearing
     *         The bearing to an enemy.
     */
    private void findEnemy(double bearing) {
        //  TODO : Performance : Improve finding an enemy: http://mark.random-article.com/weber/java/robocode/lesson4.html
        turnGunRight(getHeading() - getGunHeading() + bearing);
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

        if (attackMode == AttackModes.ONE_ON_ONE) {
            if (distance <= battleFieldSizeAverage / 3) {
                power = 3;
            }
        } else {
            if (distance <= battleFieldSizeAverage / 4) {
                power = Rules.MAX_BULLET_POWER;
            } else if (getEnergy() < minimumEnergyToFireSmallestBullets) {
                power = 0.5;
            } else if (getEnergy() < minimumEnergyToFireBigBullets) {
                power = 1;
            } else if (distance <= battleFieldSizeAverage / 3) {
                power = 2.5;
            } else if (distance <= battleFieldSizeAverage / 2) {
                power = 2;
            }

            if (power == 1 && getOthers() == 1) {
                power = 2;
            }
        }

        return power;
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

    /**
     * Handle moving ahead and back taking into account timing and rotation degrees.
     *
     * @param direction
     *         The {@link Direction} to move.
     * @param distanceToMove
     *         The distance to move.
     */
    private void handleMovement(Direction direction, double distanceToMove) {
        final double partialDistanceToMove = ROBOT_SIZE * 2;

        for (double still = distanceToMove; still > 0; still -= partialDistanceToMove) {
            switch (direction) {
                case AHEAD:
                    ahead(partialDistanceToMove);
                    break;
                case BACK:
                    back(partialDistanceToMove);
                    break;
            }

            turnGunLeft(45);
            //  TODO : Performance : Check degrees
        }
    }


    /**
     * Represents the different kind of directions that the robot can take when moving on the battlefild.
     * <p/>
     * Created on 8/13/13, at 9:36 PM.
     *
     * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
     */
    public enum Direction {
        AHEAD,
        BACK
    }

    /**
     * Represents the different kind of attacks that this robot support.
     * <p/>
     * Created on 8/14/13, at 15:36 PM.
     *
     * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
     */
    public enum AttackModes {
        TARGETING,
        ONE_ON_ONE,
        EVERYBODY_AGAINST_EVERYBODY
    }
}
