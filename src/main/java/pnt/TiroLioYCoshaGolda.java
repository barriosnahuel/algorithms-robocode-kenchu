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

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * Main class for this robot. It has the logic needed to win lot of battles... xD
 * <p/>
 * Created on 20/08/13, at 13:03.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 */
public class TiroLioYCoshaGolda extends robocode.Robot {

    //  ************************************ Constants
    protected static double ROBOT_SIZE = 36;

    protected static final double WALL_PROXIMITY_CONSTANT = 0.2;

    protected static final double LIMIT_TO_CONSIDER_STATIC_TARGET = 2;

    //  ************************************ Attack modes

    protected static final int ATTACK_MODE_SURVIVE = 0;

    protected static final int ATTACK_MODE_STAY_ALIVE = 1;

    protected static final int ATTACK_MODE_MELEE = 2;

    protected static final int ATTACK_MODE_STATIC_TARGET = 3;

    protected static final int ATTACK_MODE_DYNAMIC_TARGET = 4;

    protected static final int ATTACK_MODE_ONE_ON_ONE = 5;

    //  ************************************ Properties
    protected double battleFieldSizeAverage;

    protected double minimumEnergyToSurvive;

    protected double minimumEnergyToStayAlive;

    protected double minimumEnergyToFireBigBullets;

    protected double minimumEnergyToChase;

    protected int attackMode;

    //    ********************************** Enums
    //    ***********
    //    How to set the body respect another object: wall/enemy/bullet
    protected static final int BODY_HEADING = 1;

    protected static final int BODY_PERPENDICULAR = 2;


    //  ************************************ Statistics
    protected int firedBullets;

    protected int hitBullets;

    protected int missedBullets;

    protected int notFiredBullets;

    private int hitWall;

    protected double calculateBestPowerForShooting(ScannedRobotEvent event) {
        double distance = event.getDistance();
        double myEnergy = getEnergy();

        double power = 1;

        if ((myEnergy < minimumEnergyToStayAlive && distance < ROBOT_SIZE) || attackMode == ATTACK_MODE_STATIC_TARGET) {
            power = Rules.MAX_BULLET_POWER;
        } else if (myEnergy < minimumEnergyToStayAlive) {
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

    /**
     * Handles the fire action to get statistics about fired bullets.
     *
     * @param event
     */
    protected void handleFire(ScannedRobotEvent event) {
        if (fireBullet(calculateBestPowerForShooting(event)) != null) {
            firedBullets++;
        } else {
            System.out.println("Can't fire! (time n°: " + ++notFiredBullets + ")");
        }
    }

    protected void turnGun(double absolute, double gunHeading) {
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
                turnGunRight(360 - gunHeading + absolute);
            }
        } else {
            if (angle > 0) {
                turnGunRight(absolute);
            } else if (angle > -180) {
                turnGunRight(Math.abs(angle));//  da negativo y va para la izquierda
            } else {
                turnGunRight(360 - Math.abs(angle));
            }
        }
    }

    protected void turnRadar(double absolute, double radarHeading) {
        double angle = absolute - radarHeading;

        if (absolute > 0) {
            if (angle > 180) {
                turnRadarLeft(360 - absolute + radarHeading);
            } else if (angle > 0) {
                //  y menor a 180, por la condicion anterior
                turnRadarRight(angle);
            } else if (angle < 0 && angle > -180) {
                turnRadarRight(angle);//  da negativo y va para la izquierda
            } else if (angle < -180) {
                turnRadarRight(360 - radarHeading + absolute);
            }
        } else {
            if (angle > 0) {
                turnRadarRight(absolute);
            } else if (angle > -180) {
                turnRadarRight(Math.abs(angle));//  da nega
                // tivo y va para la izquierda
            } else {
                turnRadarRight(360 - Math.abs(angle));
            }
        }
    }

    /**
     * Rotates the body of the robot to stay horizontally to the specified robot/wall/bullet origin.
     *
     * @param mode
     *         The final desired state of the body against the robot/wall/bullet: BODY_HEADING or BODY_PERPENDICULAR.
     * @param bearing
     *         The bearing to the robot/wall/bullet origin, in degrees.
     */
    protected void turnBody(int mode, double bearing) {
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

    protected void scanForEnemies(int steps, boolean turnLeft) {
        double degrees = 360 / steps;
        for (double left = 360; left > 0; left -= degrees) {
            if (turnLeft) {
                turnRadarLeft(degrees);
            } else {
                turnRadarLeft(degrees);
            }
        }
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
    protected boolean isHeadingWall(double x, double y) {
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

    protected boolean isNearBottomLeftCorner(double x, double y) {
        return x < getBattleFieldWidth() * WALL_PROXIMITY_CONSTANT && y < getBattleFieldHeight() * WALL_PROXIMITY_CONSTANT;
    }

    protected boolean isNearBottomRightCorner(double x, double y) {
        return x > getBattleFieldWidth() * (1 - WALL_PROXIMITY_CONSTANT) && y < getBattleFieldHeight() * WALL_PROXIMITY_CONSTANT;
    }

    protected boolean isNearTopRightCorner(double x, double y) {
        return x > getBattleFieldWidth() * (1 - WALL_PROXIMITY_CONSTANT) && y > getBattleFieldHeight() * (1 - WALL_PROXIMITY_CONSTANT);
    }

    protected boolean isNearTopLeftCorner(double x, double y) {
        return x < getBattleFieldWidth() * WALL_PROXIMITY_CONSTANT && y > getBattleFieldHeight() * (1 - WALL_PROXIMITY_CONSTANT);
    }

    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);

        System.out.println("---------------------");
        System.out.println("Fired bullets: " + firedBullets);
        System.out.println("Hit bullets: " + hitBullets);
        System.out.println("Missed bullets: " + missedBullets);
        System.out.println("Not fired bullets: " + notFiredBullets);
        System.out.println("Hit wall: " + hitWall);
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        missedBullets++;
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        super.onBulletHitBullet(event);
        missedBullets++;
    }

    //  --------------------------------------------------------------
    //  --------------------------------------------------------------
    //  --------------------------------------------------------------
    //  --------------------------------------------------------------
    //  --------------------------------------------------------------
    //  --------------------------------------------------------------
    //  --------------------------------------------------------------


    //  **********************************************************************
    //  Melee fields
    private boolean isAttacking;

    private double targetDistance;

    //  **********************************************************************
    //  One on one fields
    private static final double DISTANCE_TO_MOVE_ONE_ON_ONE = ROBOT_SIZE * 4;

    //  **********************************************************************
    //  Dynamic target fields
    private static final int RECEIVED_BULLETS_LIMIT = 4;

    private int receivedBullets;

    private String lastBulletOwner;

    private String targetName;

    private double targetEnergy;

    private boolean hasTarget;

    private boolean isDynamicModeStarted;

    private int previousAttackMode;

    @Override
    public void run() {
        double initialEnergy = getEnergy();

        minimumEnergyToSurvive = initialEnergy * 0.15;
        minimumEnergyToStayAlive = initialEnergy * 0.25;
        minimumEnergyToFireBigBullets = initialEnergy * 0.35;
        minimumEnergyToChase = initialEnergy * 0.45;
        battleFieldSizeAverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        while (true) {
            setAttackMode();
            move();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        setAttackMode(event);

        switch (attackMode) {
            case ATTACK_MODE_SURVIVE:
                System.out.println("onScannedRobot for SURVIVE");
                //  TODO : Functionality : onScannedRobotForSurvive: When enemies energies is too much more than mine, continue attacking because I wil die after all.
                handleFire(event);
                break;
            case ATTACK_MODE_STAY_ALIVE:
                System.out.println("onScannedRobot for STAY ALIVE");
                //  TODO : Functionality : onScannedRobotForStayAlive: Fire only against enemies with less energy than me.
                handleFire(event);
                break;
            case ATTACK_MODE_STATIC_TARGET:
                System.out.println("onScannedRobot for STATIC TARGET");
                onScannedRobotForDynamicTarget(event);
            case ATTACK_MODE_DYNAMIC_TARGET:
                System.out.println("onScannedRobot for DYNAMIC TARGET");
                onScannedRobotForDynamicTarget(event);
                break;
            case ATTACK_MODE_ONE_ON_ONE:
                System.out.println("onScannedRobot for ONE ON ONE");
                onScannedRobotForOneOnOne(event);
                break;
            case ATTACK_MODE_MELEE:
            default:
                System.out.println("onScannedRobot for MELEE");
                onScannedRobotForMelee(event);
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        hitBullets++;

        if (attackMode == ATTACK_MODE_STATIC_TARGET || attackMode == ATTACK_MODE_DYNAMIC_TARGET) {
            targetEnergy = event.getEnergy();
        }
    }

    private void restoreAttackMode() {
        attackMode = previousAttackMode;
        previousAttackMode = -1;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        hitWall++;

        switch (attackMode) {
            case ATTACK_MODE_ONE_ON_ONE:
                System.out.println("onHitWall for ONE ON ONE");
                onHitWallForOneOnOne(event);
                break;
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        super.onHitByBullet(event);

        String enemyName = event.getName();
        if (lastBulletOwner == null || enemyName.equals(lastBulletOwner)) {
            lastBulletOwner = enemyName;

            if (++receivedBullets >= RECEIVED_BULLETS_LIMIT) {
                setAttackMode();
                if (!isDynamicModeStarted && hasTarget && attackMode == ATTACK_MODE_DYNAMIC_TARGET) {
                    System.out.println("onHitByBullet for DYNAMIC TARGET");
                    startDynamicTargetMode(event.getBearing());
                    isDynamicModeStarted = true;
                }
            }
        } else {
            lastBulletOwner = enemyName;
            receivedBullets = 1;
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event);

        switch (attackMode) {
            case ATTACK_MODE_STATIC_TARGET:
            case ATTACK_MODE_DYNAMIC_TARGET:
                if (event.getName().equals(targetName)) {
                    //  Clean target information.
                    targetName = null;
                    targetEnergy = -1;
                    isDynamicModeStarted = false;

                    stop();
                    restoreAttackMode();
                }
        }
    }

    private void onScannedRobotForDynamicTarget(ScannedRobotEvent event) {
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

    private void onHitWallForOneOnOne(HitWallEvent event) {
        turnBody(BODY_PERPENDICULAR, event.getBearing());
        if (isHeadingWall(getX(), getY())) {
            back(ROBOT_SIZE * 2);
        } else {
            ahead(ROBOT_SIZE * 2);
        }
    }

    private void onScannedRobotForOneOnOne(ScannedRobotEvent event) {
        double bearing = event.getBearing();

        if (getEnergy() >= minimumEnergyToSurvive && getGunHeat() == 0) {
            double heading = getHeading();
            turnGun(heading + bearing, getGunHeading());
            handleFire(event);
            turnRadar(heading + bearing, getRadarHeading());
            if (Math.abs(bearing) != 90) {
                turnBody(BODY_PERPENDICULAR, bearing);
            }
        }

        turnBody(BODY_PERPENDICULAR, bearing);
    }

    private void setAttackMode() {
        double myEnergy = getEnergy();

        if (myEnergy <= minimumEnergyToSurvive) {
            attackMode = ATTACK_MODE_SURVIVE;
            System.out.println("Attack mode set to: SURVIVE");
        } else if (myEnergy <= minimumEnergyToStayAlive) {
            attackMode = ATTACK_MODE_STAY_ALIVE;
            System.out.println("Attack mode set to: STAY ALIVE");
        } else if (receivedBullets >= RECEIVED_BULLETS_LIMIT) {
            if (attackMode != ATTACK_MODE_DYNAMIC_TARGET) {
                previousAttackMode = attackMode;
                attackMode = ATTACK_MODE_DYNAMIC_TARGET;
            }
            System.out.println("Attack mode set to: DYNAMIC TARGET");
        } else if (getOthers() > 1 && !hasTarget) {
            attackMode = ATTACK_MODE_MELEE;
            System.out.println("Attack mode set to: MELEE");
        } else if (!hasTarget) {
            attackMode = ATTACK_MODE_ONE_ON_ONE;
            System.out.println("Attack mode set to: ONE ON ONE");
        }

        hasTarget = attackMode == ATTACK_MODE_DYNAMIC_TARGET;
    }

    private void setAttackMode(ScannedRobotEvent event) {
        double myEnergy = getEnergy();
        setAttackMode();

        boolean isStatic = event.getVelocity() <= LIMIT_TO_CONSIDER_STATIC_TARGET;
        if ((isStatic && myEnergy >= minimumEnergyToChase) || (isStatic && event.getEnergy() < myEnergy)) {
            if (attackMode != ATTACK_MODE_STATIC_TARGET) {
                previousAttackMode = attackMode;
                attackMode = ATTACK_MODE_STATIC_TARGET;
                System.out.println("Attack mode set to: STATIC TARGET");
                hasTarget = true;
            }
        }

//        if (event.getEnergy() <= minimumEnergyToChase && getEnergy() >= event.getEnergy()) {
//            previousAttackMode = attackMode;
//            attackMode = ATTACK_MODE_DYNAMIC_TARGET;
//            System.out.println("Attack mode set to: DYNAMIC TARGET");
//            hasTarget = true;
//        }
    }

    private void move() {
        switch (attackMode) {
            case ATTACK_MODE_SURVIVE:
                System.out.println("Moving in circles for attack mode SURVIVE");
                setColors(Color.red, Color.red, Color.red);
                moveInCircles();
                break;
            case ATTACK_MODE_STAY_ALIVE:
                System.out.println("Moving in circles for attack mode STAY ALIVE");
                setColors(Color.orange, Color.orange, Color.orange);
                moveInCircles();
                break;
            case ATTACK_MODE_DYNAMIC_TARGET:
                System.out.println("Move for DYNAMIC TARGET");
                moveForDynamicTarget();
                break;
            case ATTACK_MODE_ONE_ON_ONE:
                System.out.println("Move for ONE ON ONE");
                moveForOneOnOne();
                break;
            case ATTACK_MODE_MELEE:
                System.out.println("Move for MELEE");
                moveForMelee();
        }
    }

    private void moveInCircles() {
        if (getGunHeading() != getRadarHeading()) {
            turnRadar(getGunHeading(), getRadarHeading());
        }

        ahead(ROBOT_SIZE * 3);
        turnRight(30);
    }

    private void moveForOneOnOne() {
        setColors(Color.green, Color.black, Color.green);

        //  TODO : Improve this degrees. Turn 360 only the first time.

        turnRadarRight(360);
        ahead(DISTANCE_TO_MOVE_ONE_ON_ONE);
        turnRadarRight(360);
        back(DISTANCE_TO_MOVE_ONE_ON_ONE);
    }

    private void moveForDynamicTarget() {
        setColors(Color.black, Color.black, Color.white);

        //  TODO : Improve this degrees.

        turnRadarRight(360);
        turnRadarLeft(90);
    }

    private void startDynamicTargetMode(double bearing) {
        setColors(Color.black, Color.black, Color.white);

        turnRadar(getHeading() + bearing, getRadarHeading());
    }

    private void moveForMelee() {
        setColors(Color.white, Color.white, Color.black);

        scanForEnemies(4, false);
        ahead(ROBOT_SIZE * 4);
        scanForEnemies(4, false);
        back(ROBOT_SIZE * 4);
    }

    private void onScannedRobotForMelee(ScannedRobotEvent event) {
        double enemyDistance = event.getDistance();
        if (!isAttacking || enemyDistance < targetDistance) {
            stop();
            isAttacking = true;
            targetDistance = enemyDistance;

            turnGun(getHeading() + event.getBearing(), getGunHeading());

            handleFire(event);
            resume();
        }

        isAttacking = false;
    }
}
