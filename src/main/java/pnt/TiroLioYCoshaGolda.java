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

    private boolean hasTarget;

    private double distanceToMoveWhenOneOnOne;

    private boolean isTarget;

    private boolean isScanningEnemy = true;

    private BattleMode battleMode = BattleMode.EVERYBODY_AGAINST_EVERYBODY;

    @Override
    public void run() {
        initialEnergy = getEnergy();
        minimumEnergyToRam = initialEnergy / 2;
        minimumEnergyToFireBigBullets = initialEnergy * 0.3;
        minimumEnergyToFireSmallestBullets = initialEnergy * 0.1;

        final int degreesToRotateGun = 20;

        battleFieldSizeAverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;
        final double distanceToGoAhead = battleFieldSizeAverage / 5;
        final double distanceToGoBack = distanceToGoAhead / 2;
        distanceToMoveWhenOneOnOne = battleFieldSizeAverage / 3;

        setColors(Color.black, Color.black, Color.green);
        setBulletColor(Color.cyan);

        //noinspection InfiniteLoopStatement
        while (true) {

            if (getOthers() == 1) {
                battleMode = BattleMode.ONE_ON_ONE;
                battleVsOneOpponent();
            } else {
                battleVsVariousOpponents(distanceToGoAhead, distanceToGoBack);
            }
        }
    }

    /**
     * Runs the main strategy when battling against only one enemy.
     */
    private void battleVsOneOpponent() {
        if (isScanningEnemy) {
            turnRadarRight(360);
        }

        ahead(distanceToMoveWhenOneOnOne);
        back(distanceToMoveWhenOneOnOne);
    }

    /**
     * TODO : Javadoc for battleVsVariousOpponents
     *
     * @param distanceToGoAhead
     * @param distanceToGoBack
     */
    private void battleVsVariousOpponents(double distanceToGoAhead, double distanceToGoBack) {
        handleMovement(Direction.AHEAD, distanceToGoAhead);
        scanForEnemies(40, true);
        handleMovement(Direction.BACK, distanceToGoBack);
        scanForEnemies(40, true);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        handleAttackStrategy(event);
    }

    /**
     * Handle the attacking strategy when receiving a {@link ScannedRobotEvent}.
     *
     * @param event
     *         The The {@link ScannedRobotEvent}.
     */
    private void handleAttackStrategy(ScannedRobotEvent event) {
        System.out.println("Scanned robot: " + event.getName() + " (" + event.getEnergy() + ")");

        if (battleMode == BattleMode.ONE_ON_ONE) {
            if (isScanningEnemy) {
                isScanningEnemy = false;
                handleOneOnOneAttackStrategyForFirstMove(event);
            } else {
                if (getGunHeat() > 0) {
                    turnGunLeft(5);
                    turnGunRight(10);
                    turnGunLeft(5);
                }
                handleFire(calculateBestPowerForShooting(event));
            }
        } else {
            boolean enemyIsNotMoving = event.getVelocity() < 2;
            if (enemyIsNotMoving && !hasTarget) {
                hasTarget = true;
            }

            if (getGunHeat() == 0 || hasTarget) {
                boolean attack = true;

                if (isEscaping) {
                    //  TODO : Functionality : If I can kill enemy with just one or two bullets, then kill him.

                    if ((getEnergy() < initialEnergy * 0.3 || event.getEnergy() > initialEnergy * 0.2) && battleMode != BattleMode.ONE_ON_ONE) {
                        System.out.println("I'm escaping from an enemy, then I won't stay quite to attack another one.");
                        attack = false;
                    }
                }

                if (attack) {
                    if (enemyIsNotMoving) {
                        hasTarget = true;
                        attackStaticEnemy();
                    } else {
                        hasTarget = false;
                        handleFire(calculateBestPowerForShooting(event));
                    }
                }
            } else {
                System.out.println("Gun is " + getGunHeat() + " heat: skipping trying to fire.");
            }
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
    private void handleOneOnOneAttackStrategyForFirstMove(ScannedRobotEvent event) {
        stop();

        //  TODO : Functionality : Sets robot horizontally to escape easy.

        setAdjustRadarForGunTurn(true);
        double bearing = event.getBearing();
        turnGunRight(bearing);
        setAdjustRadarForGunTurn(false);

        if (bearing > 0) {
            turnRadarLeft(getRadarHeading() - getGunHeading());
        } else {
            turnRadarRight(getGunHeading() - getRadarHeading());
        }

        handleFire(calculateBestPowerForShooting(event));
//        resume();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        isEscaping = true;
        hasTarget = false;
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
     * TODO : Javadoc for getDistanceToRun
     *
     * @param directionToRun
     *
     * @return
     */
    private double getDistanceToRun(Direction directionToRun) {
        //  Default is for run ahead.
        double distance = DISTANCE_TO_RUN;

        if (isTarget) {
            distance = battleFieldSizeAverage * 0.6;
        } else if (directionToRun == Direction.BACK) {
            distance = DISTANCE_TO_RUN_BACK;
        }

        return distance;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        final double degreesToGoOut = 90;

        boolean turnRight = changeDirection(event.getBearing(), degreesToGoOut);

        if (battleMode == BattleMode.ONE_ON_ONE) {
            isScanningEnemy = true;
            battleVsOneOpponent();
        } else {
            ahead(ROBOT_SIZE);

            if (turnRight) {
                turnRight(degreesToGoOut);
            } else {
                turnLeft(degreesToGoOut);
            }
        }
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
     */
    private void attackStaticEnemy() {
        System.out.println("Attacking a static enemy.");

        handleFire(Rules.MAX_BULLET_POWER);

        stop(true);

        if (isEscaping) {
            System.out.println("I'm escaping from an enemy, then I won't stay quite to attack another one.");
            resume();
        } else {
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

        if (isTarget) {
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
     * TODO : Javadoc for BattleMode
     * <p/>
     * Created on 8/14/13, at 15:36 PM.
     *
     * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
     */
    public enum BattleMode {
        ONE_ON_ONE,
        EVERYBODY_AGAINST_EVERYBODY
    }
}
