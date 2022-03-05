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

package arun.com.chromer.settings.browsingmode;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.settings.widgets.IconSwitchPreference;

public class BrowseFasterPreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

  private MaterialDialog dialog;

  public BrowseFasterPreferenceFragment() {
    // Required empty public constructor
  }

  public static BrowseFasterPreferenceFragment newInstance() {
    final BrowseFasterPreferenceFragment fragment = new BrowseFasterPreferenceFragment();
    final Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.browse_faster_options);
    setupAmpPreference();
    setupArticlePreference();
  }

  private void setupArticlePreference() {
    IconSwitchPreference articleModePreference = (IconSwitchPreference) findPreference(Preferences.ARTICLE_MODE);
    if (articleModePreference != null) {
      final Drawable articleImg = new IconicsDrawable(getActivity())
        .icon(CommunityMaterial.Icon.cmd_file_document)
        .color(ContextCompat.getColor(getActivity(), R.color.android_green))
        .sizeDp(24);
      articleModePreference.setIcon(articleImg);
      articleModePreference.setOnPreferenceClickListener(preference -> false);
      articleModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
        showInformationDialog(Preferences.get(getActivity()).ampMode(), (Boolean) newValue);
        return true;
      });
    }
  }

  private void setupAmpPreference() {
    IconSwitchPreference ampModePreference = (IconSwitchPreference) findPreference(Preferences.AMP_MODE);
    if (ampModePreference != null) {
      ampModePreference.setIcon(R.drawable.ic_action_amp_icon);
      ampModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
        final boolean isSlideOver = !Preferences.get(getContext()).webHeads();
        if (isSlideOver && (Boolean) newValue) {
          new MaterialDialog.Builder(getActivity())
            .title(R.string.amp_warning_title)
            .content(R.string.amp_warning_content, true)
            .positiveText(android.R.string.ok)
            .iconRes(R.drawable.ic_action_amp_icon)
            .show();
        }
        showInformationDialog((Boolean) newValue, Preferences.get(getActivity()).articleMode());
        return true;
      });
    }
  }

  private void showInformationDialog(final boolean ampMode, final boolean article) {
    dismissDialog();
    if (ampMode && article) {
      dialog = new MaterialDialog.Builder(getActivity())
        .iconRes(R.drawable.ic_action_amp_icon)
        .title(R.string.attention)
        .content(R.string.amp_article_combined_explanation, true)
        .positiveText(android.R.string.ok)
        .show();
    }
  }

  private void dismissDialog() {
    if (dialog != null) {
      dialog.dismiss();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
  }
}
