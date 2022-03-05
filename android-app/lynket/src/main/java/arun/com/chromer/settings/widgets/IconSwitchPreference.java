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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

/**
 * A helper preference view without weird margin on preference icon. Attempts to alter properties on
 * icon frame and icon itself so that it looks better
 */
public class IconSwitchPreference extends SwitchPreference {
  private boolean hideSwitch;

  public IconSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public IconSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public IconSwitchPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public IconSwitchPreference(Context context) {
    super(context);
  }

  @Override
  public void onBindViewHolder(PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    PreferenceIconLayoutHelper.applyLayoutChanges(holder, isEnabled());
    final View switchView = holder.findViewById(androidx.preference.R.id.switchWidget);
    if (hideSwitch && switchView != null) {
      switchView.setVisibility(View.GONE);
    }
  }

  public void hideSwitch() {
    hideSwitch = true;
  }

  public void showSwitch() {
    hideSwitch = false;
  }
}
