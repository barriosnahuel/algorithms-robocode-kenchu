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

import java.awt.*;

/**
 * TODO : Javadoc for
 * <p/>
 * Created on 8/18/13, at 4:10 PM.
 *
 * @author Nahuel Barrios <barrios.nahuel@gmail.com>.
 */
public class OneOnOne extends BaseRobot {

    @Override
    public void run() {
        super.run();

        setColors(Color.black, Color.black, Color.green);

        double distanceToMove = ROBOT_SIZE * 6;

        while (true) {
            turnRadarRight(360);
            ahead(distanceToMove);
            turnRadarRight(360);
            back(distanceToMove);
        }
    }
}
