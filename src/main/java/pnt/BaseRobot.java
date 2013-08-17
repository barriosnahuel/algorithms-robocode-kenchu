package pnt;

import robocode.Robot;
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

    //  ************************************ Properties
    protected double battleFieldSizeAverage;

    protected double minimumEnergyToStayAlive;

    protected double minimumEnergyToFireBigBullets;

    //  ************************************ Statistics
    private int firedBullets;

    private int notFiredBullets;

    @Override
    public void run() {
        double initialEnergy = getEnergy();
        minimumEnergyToStayAlive = initialEnergy * 0.15;
        battleFieldSizeAverage = getBattleFieldWidth() + getBattleFieldHeight() / 2;

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
    }

    protected abstract double calculateBestPowerForShooting(ScannedRobotEvent event);

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
                turnGunLeft(360 - gunHeading + absolute);
            }
        } else {
            turnGunRight(absolute);
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
                turnRadarLeft(360 - radarHeading + absolute);
            }
        } else {
            turnRadarRight(absolute);
        }
    }
}
