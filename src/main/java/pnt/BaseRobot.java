package pnt;

import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;

/**
 * TODO : Javadoc for
 * <p/>
 * Created on 16/08/13, at 12:42.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 * @since 26.
 */
public abstract class BaseRobot extends Robot {

    //  ************************************ Constants
    protected double ROBOT_SIZE = 36;

    protected static final double WALL_PROXIMITY_CONSTANT = 0.2;

    //  ************************************ Properties
    protected double battleFieldSizeAverage;

    protected double minimumEnergyToSurvive;

    protected double minimumEnergyToStayAlive;

    protected double minimumEnergyToFireBigBullets;

    //    ********************************** Enums
    //    ***********
    //    How to set the body respect another object: wall/enemy/bullet
    protected static final int BODY_HEADING = 1;

    protected static final int BODY_PERPENDICULAR = 2;


    //  ************************************ Statistics
    private int firedBullets;

    private int notFiredBullets;

    @Override
    public void run() {
        double initialEnergy = getEnergy();

        minimumEnergyToSurvive = initialEnergy * 0.1;
        minimumEnergyToStayAlive = initialEnergy * 0.2;
        battleFieldSizeAverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
    }

    protected double calculateBestPowerForShooting(ScannedRobotEvent event) {
        double distance = event.getDistance();
        double myEnergy = getEnergy();
        double power = 1;

        if (myEnergy < minimumEnergyToStayAlive && distance < ROBOT_SIZE) {
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
}
