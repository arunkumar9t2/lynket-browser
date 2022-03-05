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

package arun.com.chromer.bubbles.webheads.ui;

import androidx.annotation.NonNull;

import com.facebook.rebound.Spring;

import arun.com.chromer.bubbles.webheads.ui.views.WebHead;

/**
 * Created by Arun on 08/08/2016.
 */
public interface WebHeadContract {
  void onWebHeadClick(@NonNull WebHead webHead);

  void onWebHeadDestroyed(@NonNull WebHead webHead, boolean isLastWebHead);

  void onMasterWebHeadMoved(int x, int y);

  @NonNull
  Spring newSpring();

  void onMasterLockedToTrashy();

  void onMasterReleasedFromTrashy();

  void closeAll();

  void onMasterLongClick();
}
