/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.bubbles.webheads.physics;

import com.facebook.rebound.SpringConfig;

/**
 * Created by Arun on 06/08/2016.
 */

public class SpringConfigs {
  public static final SpringConfig FLING = SpringConfig.fromOrigamiTensionAndFriction(50, 5);
  public static final SpringConfig DRAG = SpringConfig.fromOrigamiTensionAndFriction(0, 1.8);
  public static final SpringConfig SNAP = SpringConfig.fromOrigamiTensionAndFriction(100, 7);

  private SpringConfigs() {
    throw new AssertionError("no instances");
  }
}
