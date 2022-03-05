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

package arun.com.chromer.settings.widgets;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceViewHolder;

import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.util.Utils;
import timber.log.Timber;

/**
 * Created by Arun on 23/06/2016.
 */
class PreferenceIconLayoutHelper {

  @ColorInt
  private static final int CHECKED_COLOR = Color.parseColor("#757575");
  @ColorInt
  private static final int UNCHECKED_COLOR = Color.parseColor("#C5C5C5");

  private PreferenceIconLayoutHelper() {
    throw new AssertionError();
  }

  /**
   * Applies layout changes on the preference icon view so that it does not look overly big.
   *
   * @param holder  the preference view holder
   * @param checked if the preference is enabled
   */
  static void applyLayoutChanges(@NonNull PreferenceViewHolder holder, boolean checked) {
    try {
      final LinearLayout iconFrame = (LinearLayout) holder.findViewById(androidx.preference.R.id.icon_frame);
      final ImageView imageView = (ImageView) holder.findViewById(android.R.id.icon);

      if (iconFrame.getMinimumWidth() != 0) {
        iconFrame.setMinimumWidth(0);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int dp12 = Utils.dpToPx(12);
        params.setMargins(dp12, 0, dp12, 0);
        imageView.setLayoutParams(params);
      }

      applyIconTint(imageView, checked);
    } catch (Exception e) {
      Timber.e(e);
    }
  }

  /**
   * Finds the icon view and attempts to tint it based on the checked state of the preference
   *
   * @param imageView The image view of icon
   * @param checked   Whether the preference is enabled
   */
  private static void applyIconTint(@NonNull ImageView imageView, boolean checked) {
    final Drawable drawable = imageView.getDrawable();
    if (drawable != null) {
      if (drawable instanceof IconicsDrawable) {
        // Just redraw with the correct color
        imageView.setImageDrawable(((IconicsDrawable) drawable).color(checked ? CHECKED_COLOR : UNCHECKED_COLOR));
      } else {
        final Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrap, checked ? CHECKED_COLOR : UNCHECKED_COLOR);
        imageView.setImageDrawable(drawable);
      }
    }
  }
}
