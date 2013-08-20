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

import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * TODO : Javadoc for
 * <p/>
 * Created on 20/08/13, at 13:03.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 */
public class TiroLioYCoshaGolda extends BaseRobot {

    //  **********************************************************************
    //  Attack modes

    private static final int ATTACK_MODE_SURVIVE = 0;

    private static final int ATTACK_MODE_STAY_ALIVE = 1;

    private static final int ATTACK_MODE_MELEE = 2;

    private static final int ATTACK_MODE_DYNAMIC_TARGET = 3;

    private static final int ATTACK_MODE_ONE_ON_ONE = 4;

    //  **********************************************************************
    //  Melee fields
    private boolean isAttacking;

    private double targetDistance;

    //  **********************************************************************
    //  One on one fields
    private static final double DISTANCE_TO_MOVE_ONE_ON_ONE = ROBOT_SIZE * 4;

    @Override
    public void run() {
        super.run();

        attackMode = ATTACK_MODE_MELEE;

        while (true) {
            setAttackMode();
            move();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        switch (attackMode) {
            case ATTACK_MODE_SURVIVE:
                System.out.println("onScannedRobot for SURVIVE");
                break;
            case ATTACK_MODE_STAY_ALIVE:
                System.out.println("onScannedRobot for STAY ALIVE");
                break;
            case ATTACK_MODE_DYNAMIC_TARGET:
                System.out.println("onScannedRobot for DYNAMIC TARGET");
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
    public void onHitWall(HitWallEvent event) {
        switch (attackMode) {
            case ATTACK_MODE_SURVIVE:
                System.out.println("onHitWall for SURVIVE");
                break;
            case ATTACK_MODE_STAY_ALIVE:
                System.out.println("onHitWall for STAY ALIVE");
                break;
            case ATTACK_MODE_DYNAMIC_TARGET:
                System.out.println("onHitWall for DYNAMIC TARGET");
                break;
            case ATTACK_MODE_ONE_ON_ONE:
                System.out.println("onHitWall for ONE ON ONE");
                onHitWallForOneOnOne(event);
                break;
            case ATTACK_MODE_MELEE:
                System.out.println("onHitWall for MELEE");
                break;
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
        } else if (getOthers() > 1) {
            attackMode = ATTACK_MODE_MELEE;
            System.out.println("Attack mode set to: MELEE");
        } else {
            attackMode = ATTACK_MODE_ONE_ON_ONE;
            System.out.println("Attack mode set to: ONE ON ONE");
        }
    }

    private void move() {
        switch (attackMode) {
            case ATTACK_MODE_SURVIVE:
                System.out.println("Moving in circles for attack mode SURVIVE");
                moveInCircles();
                break;
            case ATTACK_MODE_STAY_ALIVE:
                System.out.println("Moving in circles for attack mode STAY ALIVE");
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
        ahead(ROBOT_SIZE * 2);
        turnRight(30);
    }

    private void moveForOneOnOne() {
        setColors(Color.black, Color.black, Color.green);

        turnRadarRight(360);
        ahead(DISTANCE_TO_MOVE_ONE_ON_ONE);
        turnRadarRight(360);
        back(DISTANCE_TO_MOVE_ONE_ON_ONE);
    }

    private void moveForDynamicTarget() {
        setColors(Color.black, Color.black, Color.white);

        turnRadarRight(360);
        turnRadarLeft(90);
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
