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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import arun.com.chromer.R;

/**
 * Created by Arun on 16/04/2016.
 */
public class ColorPreference extends Preference {
  private View colorIndicator;
  @ColorInt
  private int color = 0;

  private int defaultColor = 0;

  public ColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  public ColorPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public ColorPreference(Context context) {
    super(context);
    init(context, null);
  }

  private void init(Context context, AttributeSet attrs) {
    setWidgetLayoutResource(R.layout.widget_color_preference);

    defaultColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);

    if (attrs != null) {
      TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ColorPreference);
      int colorRes = ta.getResourceId(R.styleable.ColorPreference_color, 0);
      if (colorRes != 0) {
        color = ContextCompat.getColor(getContext(), colorRes);
      }
      ta.recycle();
    }
  }

  @Override
  protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    if (restorePersistedValue) {
      color = this.getPersistedInt(defaultColor);
    } else {
      color = (int) defaultValue;
      persistInt(color);
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getInteger(index, defaultColor);
  }

  @Override
  public void onBindViewHolder(PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    colorIndicator = holder.findViewById(R.id.color_preview);
    PreferenceIconLayoutHelper.applyLayoutChanges(holder, isEnabled());
    invalidate();
  }

  private void invalidate() {
    if (colorIndicator != null) {
      colorIndicator.setBackgroundColor(color);
    }
  }

  @ColorInt
  public int getColor() {
    return getPersistedInt(color);
  }

  public void setColor(@ColorInt int color) {
    this.color = color;
    persistInt(color);
    setSummary(colorHexValue());
    invalidate();
  }

  private String colorHexValue() {
    return String.format("#%06X", (0xFFFFFF & color));
  }

  public void refreshSummary() {
    setSummary(colorHexValue());
  }
}
