/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

package arun.com.chromer.webheads.physics;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

public class Coordinate {
    private float x;
    private float y;

    Coordinate() {

    }

    private Coordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate FromMotionEvent(@NonNull MotionEvent event) {
        return new Coordinate(event.getRawX(), event.getRawY());
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
