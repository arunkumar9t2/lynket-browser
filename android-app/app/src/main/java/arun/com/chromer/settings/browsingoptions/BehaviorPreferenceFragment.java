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

package arun.com.chromer.settings.browsingoptions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.perapp.PerAppSettingsActivity;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.settings.widgets.IconSwitchPreference;
import arun.com.chromer.util.Utils;

import static arun.com.chromer.settings.Preferences.AGGRESSIVE_LOADING;
import static arun.com.chromer.settings.Preferences.MERGE_TABS_AND_APPS;
import static arun.com.chromer.settings.Preferences.PER_APP_PREFERENCE_DUMMY;

/**
 * Created by Arun on 21/06/2016.
 */
public class BehaviorPreferenceFragment extends BasePreferenceFragment {

  private IconSwitchPreference mergeTabsPreference;

  public BehaviorPreferenceFragment() {
    // Required empty public constructor
  }

  public static BehaviorPreferenceFragment newInstance() {
    final BehaviorPreferenceFragment fragment = new BehaviorPreferenceFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.behavior_preferences);
    setupMergeTabsPreference();
    setupBlacklistPreference();

  }

  private void setupBlacklistPreference() {
    final IconSwitchPreference perAppSettingsPreference = (IconSwitchPreference) findPreference(PER_APP_PREFERENCE_DUMMY);
    if (perAppSettingsPreference != null) {
      final Drawable recentImg = new IconicsDrawable(getActivity())
          .icon(CommunityMaterial.Icon.cmd_filter_variant)
          .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
          .sizeDp(24);
      perAppSettingsPreference.setIcon(recentImg);
      perAppSettingsPreference.hideSwitch();
      perAppSettingsPreference.setOnPreferenceClickListener(preference -> {
        new Handler().postDelayed(() -> {
          final Intent perAppSettingActivity = new Intent(getActivity(), PerAppSettingsActivity.class);
          startActivity(perAppSettingActivity);
        }, 150);
        return false;
      });
    }
  }

  private void setupMergeTabsPreference() {
    mergeTabsPreference = (IconSwitchPreference) findPreference(MERGE_TABS_AND_APPS);
    if (mergeTabsPreference != null) {
      final Drawable recentImg = new IconicsDrawable(getActivity())
          .icon(CommunityMaterial.Icon.cmd_animation)
          .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
          .sizeDp(24);
      mergeTabsPreference.setIcon(recentImg);
      mergeTabsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
        if (!((Boolean) newValue) && Preferences.get(getContext()).aggressiveLoading()) {
          new MaterialDialog.Builder(getActivity())
              .title(R.string.merge_tabs_off_title)
              .content(R.string.merget_tabs_off_content)
              .positiveText(android.R.string.ok)
              .show();
          Preferences.get(getContext()).aggressiveLoading(false);
        }
        return true;
      });
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!Utils.isLollipopAbove()) {
      mergeTabsPreference.setVisible(false);
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equalsIgnoreCase(AGGRESSIVE_LOADING)) {
      if (Preferences.get(getContext()).aggressiveLoading()) {
        mergeTabsPreference.setChecked(true);
      }
    }
  }
}
