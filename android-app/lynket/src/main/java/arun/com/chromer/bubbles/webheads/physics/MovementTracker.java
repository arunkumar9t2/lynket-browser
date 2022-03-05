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

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A helper class for tracking web heads movements. This is needed to correctly apply polarity on
 * calculated velocity by velocity tracker. For example when web head is moved from left to right
 * and top to bottom, the X and Y velocity should be positive. Sometimes that is not the case with
 * raw values given by{@link android.view.VelocityTracker}
 */
public class MovementTracker {
  private static MovementTracker INSTANCE;
  private final SizedQueue<Float> xPoints;
  private final SizedQueue<Float> yPoints;
  private int trackingSize;

  private MovementTracker() {
    trackingSize = 10;
    xPoints = new SizedQueue<>(trackingSize);
    yPoints = new SizedQueue<>(trackingSize);
  }

  @NonNull
  public static MovementTracker obtain() {
    if (INSTANCE == null) {
      INSTANCE = new MovementTracker();
    }
    return INSTANCE;
  }

  public static float[] adjustVelocities(float[] p1, float[] p2, float xVelocity, float yVelocity) {
    float downX = p1[0];
    float downY = p1[1];

    float upX = p2[0];
    float upY = p2[1];

    float x = 0, y = 0;

    if (upX >= downX && upY >= downY) {
      // Bottom right
      x = positive(xVelocity);
      y = positive(yVelocity);
    } else if (upX >= downX && upY <= downY) {
      // Top right
      x = positive(xVelocity);
      y = negate(yVelocity);
    } else if (upX <= downX && upY <= downY) {
      // Top left
      x = negate(xVelocity);
      y = negate(yVelocity);
    } else if (upX <= downX && upY >= downY) {
      // Bottom left
      x = negate(xVelocity);
      y = positive(yVelocity);
    }
    return new float[]{x, y};
  }

  private static float negate(float value) {
    return value > 0 ? -value : value;
  }

  private static float positive(float value) {
    return Math.abs(value);
  }

  /**
   * Adds a motion event to the tracker.
   *
   * @param event The event to be added.
   */
  public void addMovement(@NonNull MotionEvent event) {
    float x = event.getRawX();
    float y = event.getRawY();
    xPoints.add(x);
    yPoints.add(y);
  }

  /**
   * Clear the tracking queue when user begins the gesture.
   */
  public void onDown() {
    xPoints.clear();
    yPoints.clear();
  }

  /**
   * Clear the tracking queue when user ends the gesture.
   */
  public void onUp() {
    xPoints.clear();
    yPoints.clear();
  }

  public float[] getAdjustedVelocities(float xVelocity, float yVelocity) {
    int trackingThreshold = (int) (0.25 * trackingSize);
    float[] velocities;
    if (xPoints.size() >= trackingThreshold) {
      int downIndex = xPoints.size() - trackingThreshold;

      float[] up = new float[]{xPoints.getLast(), yPoints.getLast()};
      float[] down = new float[]{xPoints.get(downIndex), yPoints.get(downIndex)};

      velocities = adjustVelocities(down, up, xVelocity, yVelocity);
    } else {
      velocities = null;
    }
    return velocities;
  }

  @Override
  public String toString() {
    return xPoints.toString() + yPoints;
  }
}

/**
 * A size limited queue structure that evicts the queue head when maximum queue size is reached. At
 * any instant the queue is equal or less than the max queue size.
 *
 * @param <E>
 */
class SizedQueue<E> extends LinkedList<E> {
  /**
   * The maximum size of queue
   */
  private final int limit;

  SizedQueue(int limit) {
    this.limit = limit;
  }

  @Override
  public boolean add(E o) {
    super.add(o);
    while (size() > limit) {
      super.remove();
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    throw new UnsupportedOperationException("Not implemented, use add()");
  }

  @Override
  public void add(int location, E object) {
    throw new UnsupportedOperationException("Not implemented, use add()");
  }

  @Override
  public void addFirst(E object) {
    throw new UnsupportedOperationException("Not implemented, use add()");
  }

  @Override
  public void addLast(E object) {
    throw new UnsupportedOperationException("Not implemented, use add()");
  }

  @Override
  public boolean addAll(int location, Collection<? extends E> collection) {
    throw new UnsupportedOperationException("Not implemented, use add()");
  }
}
