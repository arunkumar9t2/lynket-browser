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
import android.util.TypedValue;
import android.widget.TextView;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

import arun.com.chromer.util.Utils;

/**
 * Created by Arun on 19/06/2016.
 */
public class SubCheckBoxPreference extends CheckBoxPreference {
  public SubCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public SubCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public SubCheckBoxPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SubCheckBoxPreference(Context context) {
    super(context);
  }

  @Override
  public void onBindViewHolder(PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    TextView title = (TextView) holder.findViewById(android.R.id.title);
    TextView summary = (TextView) holder.findViewById(android.R.id.summary);

    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
    summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
    int dp56 = Utils.dpToPx(56);
    title.setPadding(dp56, 0, 0, 0);
    summary.setPadding(dp56, 0, 0, 0);
  }
}
