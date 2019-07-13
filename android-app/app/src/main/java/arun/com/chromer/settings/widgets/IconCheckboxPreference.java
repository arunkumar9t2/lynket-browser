/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.settings.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

import static android.view.View.GONE;

/**
 * A helper preference view without weird margin on preference icon. Attempts to alter properties on
 * icon frame and icon itself so that it looks better
 */
public class IconCheckboxPreference extends CheckBoxPreference {
    private boolean hideCheckbox;

    public IconCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IconCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IconCheckboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconCheckboxPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        PreferenceIconLayoutHelper.applyLayoutChanges(holder, isEnabled());
        View checkboxView = holder.findViewById(androidx.preference.R.id.checkbox);
        if (hideCheckbox && checkboxView != null) {
            checkboxView.setVisibility(GONE);
        }
    }

    public void hideCheckbox() {
        hideCheckbox = true;
    }

    public void showCheckbox() {
        hideCheckbox = false;
    }
}
