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

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;

import java.util.Iterator;
import java.util.LinkedList;

import arun.com.chromer.bubbles.webheads.WebHeadService;
import arun.com.chromer.util.Utils;


/**
 * Created by Arun on 06/08/2016.
 * Custom spring chain helper that simplifies maintaining 2 separate chains for X and Y axis.
 */
public class SpringChain2D implements SpringListener {
  private static final int xDiff = Utils.dpToPx(4);
  private static final int yDiff = Utils.dpToPx(1.7);
  private final LinkedList<Spring> xSprings = new LinkedList<>();
  private final LinkedList<Spring> ySpring = new LinkedList<>();
  private final int sDispWidth;
  private Spring xMasterSpring;
  private Spring yMasterSpring;
  private boolean displacementEnabled = true;

  private SpringChain2D(int dispWidth) {
    this.sDispWidth = dispWidth;
  }

  public static SpringChain2D create(Context context) {
    final DisplayMetrics metrics = new DisplayMetrics();
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    windowManager.getDefaultDisplay().getMetrics(metrics);
    return new SpringChain2D(metrics.widthPixels);
  }

  public void clear() {
    if (xMasterSpring != null) {
      xMasterSpring.removeListener(this);
    }
    if (yMasterSpring != null) {
      yMasterSpring.removeListener(this);
    }
    for (final Spring spring : xSprings) {
      spring.removeListener(this);
    }
    for (final Spring spring : ySpring) {
      spring.removeListener(this);
    }
    xSprings.clear();
    ySpring.clear();
  }

  public void setMasterSprings(@NonNull Spring xMaster, @NonNull Spring yMaster) {
    xMasterSpring = xMaster;
    yMasterSpring = yMaster;
    xMasterSpring.addListener(this);
    yMasterSpring.addListener(this);
  }

  public void addSlaveSprings(@NonNull Spring xSpring, @NonNull Spring ySpring) {
    if (xSprings.size() <= WebHeadService.MAX_VISIBLE_WEB_HEADS) {
      xSprings.add(xSpring);
      this.ySpring.add(ySpring);
    }
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    final int masterX = (int) xMasterSpring.getCurrentValue();
    final int masterY = (int) yMasterSpring.getCurrentValue();
    performGroupMove(masterX, masterY);
  }

  public void rest() {
    Iterator lit = xSprings.descendingIterator();
    while (lit.hasNext()) {
      final Spring s = (Spring) lit.next();
      s.setAtRest();
    }
    lit = ySpring.descendingIterator();
    while (lit.hasNext()) {
      final Spring s = (Spring) lit.next();
      s.setAtRest();
    }
  }

  public void performGroupMove(int masterX, int masterY) {
    int xDisplacement = 0;
    int yDisplacement = 0;

    final Iterator xIter = xSprings.descendingIterator();
    final Iterator yIter = ySpring.descendingIterator();

    while (xIter.hasNext() && yIter.hasNext()) {
      final Spring xSpring = (Spring) xIter.next();
      final Spring ySpring = (Spring) yIter.next();
      if (displacementEnabled) {
        if (isRight(masterX)) {
          xDisplacement += xDiff;
        } else {
          xDisplacement -= xDiff;
        }
        yDisplacement += yDiff;
      }
      xSpring.setEndValue(masterX + xDisplacement);
      ySpring.setEndValue(masterY + yDisplacement);
    }
  }

  /**
   * Used to determine if the given pixel is to the left or the right of the screen.
   *
   * @return true if right
   */
  private boolean isRight(int x) {
    return x > (sDispWidth / 2);
  }

  @Override
  public void onSpringAtRest(Spring spring) {

  }

  @Override
  public void onSpringActivate(Spring spring) {

  }

  @Override
  public void onSpringEndStateChange(Spring spring) {

  }

  public void disableDisplacement() {
    displacementEnabled = false;
  }

  public void enableDisplacement() {
    displacementEnabled = true;
  }
}
