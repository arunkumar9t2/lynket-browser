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

import static arun.com.chromer.settings.Preferences.ARTICLE_THEME;
import static arun.com.chromer.settings.Preferences.WEB_HEAD_ENABLED;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.settings.widgets.IconListPreference;

public class ArticlePreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

  private final String[] SUMMARY_GROUP = new String[]{
    ARTICLE_THEME
  };
  private IconListPreference spawnLocation;

  public ArticlePreferenceFragment() {
    // Required empty public constructor
  }

  public static ArticlePreferenceFragment newInstance() {
    final ArticlePreferenceFragment fragment = new ArticlePreferenceFragment();
    final Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.article_preferences);
    init();
    setIcons();
  }


  @Override
  public void onResume() {
    super.onResume();
    updatePreferenceSummary(SUMMARY_GROUP);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    updatePreferenceStates(key);
    updatePreferenceSummary(key);
  }

  private void init() {
    spawnLocation = (IconListPreference) findPreference(ARTICLE_THEME);
  }


  private void setIcons() {
    int materialLight = ContextCompat.getColor(getActivity(), R.color.material_dark_light);
    spawnLocation.setIcon(new IconicsDrawable(getActivity())
      .icon(CommunityMaterial.Icon.cmd_format_color_fill)
      .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
      .sizeDp(24));
  }

  private void updatePreferenceStates(String key) {
    if (key.equalsIgnoreCase(WEB_HEAD_ENABLED)) {
      final boolean articleMode = Preferences.get(getContext()).articleMode();
      enableDisablePreference(articleMode, SUMMARY_GROUP);
    }
  }
}
