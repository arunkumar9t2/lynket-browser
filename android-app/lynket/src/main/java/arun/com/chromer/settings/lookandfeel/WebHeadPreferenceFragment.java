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

package arun.com.chromer.settings.lookandfeel;

import static arun.com.chromer.settings.Preferences.WEB_HEADS_COLOR;
import static arun.com.chromer.settings.Preferences.WEB_HEAD_ENABLED;
import static arun.com.chromer.settings.Preferences.WEB_HEAD_SIZE;
import static arun.com.chromer.settings.Preferences.WEB_HEAD_SPAWN_LOCATION;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBHEAD_COLOR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.settings.widgets.ColorPreference;
import arun.com.chromer.settings.widgets.IconListPreference;
import arun.com.chromer.shared.Constants;

public class WebHeadPreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

  private final String[] SUMMARY_GROUP = new String[]{
    WEB_HEAD_SPAWN_LOCATION,
    WEB_HEADS_COLOR,
    WEB_HEAD_SIZE,
  };

  private final IntentFilter webHeadColorFilter = new IntentFilter(Constants.ACTION_WEBHEAD_COLOR_SET);
  private final BroadcastReceiver colorSelectionReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int selectedColor = intent.getIntExtra(EXTRA_KEY_WEBHEAD_COLOR, 0);
      if (selectedColor != 0) {
        final ColorPreference preference = (ColorPreference) findPreference(WEB_HEADS_COLOR);
        if (preference != null) {
          preference.setColor(selectedColor);
        }
      }
    }
  };
  private ColorPreference webHeadColor;
  private IconListPreference spawnLocation;
  private IconListPreference webHeadSize;

  public WebHeadPreferenceFragment() {
    // Required empty public constructor
  }

  public static WebHeadPreferenceFragment newInstance() {
    final WebHeadPreferenceFragment fragment = new WebHeadPreferenceFragment();
    final Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.webhead_preferences);
    init();
    setIcons();
    setupWebHeadColorPreference();
  }

  @Override
  public void onResume() {
    super.onResume();
    registerReceiver(colorSelectionReceiver, webHeadColorFilter);
    updatePreferenceStates(WEB_HEAD_ENABLED);
    updatePreferenceSummary(SUMMARY_GROUP);
  }

  @Override
  public void onPause() {
    unregisterReceiver(colorSelectionReceiver);
    super.onPause();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    updatePreferenceStates(key);
    updatePreferenceSummary(key);
  }

  private void init() {
    webHeadColor = (ColorPreference) findPreference(WEB_HEADS_COLOR);
    spawnLocation = (IconListPreference) findPreference(WEB_HEAD_SPAWN_LOCATION);
    webHeadSize = (IconListPreference) findPreference(WEB_HEAD_SIZE);
  }

  private void setIcons() {
    int materialLight = ContextCompat.getColor(getActivity(), R.color.material_dark_light);
    webHeadColor.setIcon(new IconicsDrawable(getActivity())
      .icon(CommunityMaterial.Icon.cmd_palette)
      .color(materialLight)
      .sizeDp(24));
    spawnLocation.setIcon(new IconicsDrawable(getActivity())
      .icon(CommunityMaterial.Icon.cmd_code_tags)
      .color(materialLight)
      .sizeDp(24));
    webHeadSize.setIcon(new IconicsDrawable(getActivity())
      .icon(CommunityMaterial.Icon.cmd_crop_free)
      .color(materialLight)
      .sizeDp(24));
  }

  private void setupWebHeadColorPreference() {
    webHeadColor.setOnPreferenceClickListener(preference -> {
      final int chosenColor = ((ColorPreference) preference).getColor();
      new ColorChooserDialog.Builder(getActivity(), R.string.web_heads_color)
        .titleSub(R.string.web_heads_color)
        .allowUserColorInputAlpha(false)
        .preselect(chosenColor)
        .dynamicButtonColor(false)
        .show(getFragmentManager());
      return true;
    });
  }

  private void updatePreferenceStates(String key) {
    if (key.equalsIgnoreCase(WEB_HEAD_ENABLED)) {
      final boolean webHeadsEnabled = Preferences.get(getContext()).webHeads();
      enableDisablePreference(webHeadsEnabled, SUMMARY_GROUP);
    }
  }
}
