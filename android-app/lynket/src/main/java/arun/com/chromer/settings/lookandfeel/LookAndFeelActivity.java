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

import static arun.com.chromer.shared.Constants.ACTION_TOOLBAR_COLOR_SET;
import static arun.com.chromer.shared.Constants.ACTION_WEBHEAD_COLOR_SET;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_TOOLBAR_COLOR;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBHEAD_COLOR;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.android.material.snackbar.Snackbar;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.shared.base.Snackable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LookAndFeelActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, Snackable, SharedPreferences.OnSharedPreferenceChangeListener {
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.coordinatorLayout)
  CoordinatorLayout coordinatorLayout;
  @BindView(R.id.error)
  TextView error;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_look_and_feel);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.toolbar_options_preferences_container, PersonalizationPreferenceFragment.newInstance())
      .replace(R.id.web_head_options_preferences_container, WebHeadPreferenceFragment.newInstance())
      .replace(R.id.article_options_preferences_container, ArticlePreferenceFragment.newInstance())
      .commit();
  }

  @Override
  protected void onResume() {
    super.onResume();
    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    showHideErrorView();
  }

  private void showHideErrorView() {
    if (!Preferences.get(this).webHeads()) {
      error.setVisibility(View.VISIBLE);
    } else {
      error.setVisibility(View.GONE);
    }
  }

  @Override
  protected void onPause() {
    PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  @Override
  public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
    switch (dialog.getTitle()) {
      case R.string.default_toolbar_color:
        final Intent toolbarColorIntent = new Intent(ACTION_TOOLBAR_COLOR_SET);
        toolbarColorIntent.putExtra(EXTRA_KEY_TOOLBAR_COLOR, selectedColor);
        LocalBroadcastManager.getInstance(this).sendBroadcast(toolbarColorIntent);
        break;
      case R.string.web_heads_color:
        final Intent webHeadColorIntent = new Intent(ACTION_WEBHEAD_COLOR_SET);
        webHeadColorIntent.putExtra(EXTRA_KEY_WEBHEAD_COLOR, selectedColor);
        LocalBroadcastManager.getInstance(this).sendBroadcast(webHeadColorIntent);
        break;
    }
  }

  @Override
  public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

  }

  @Override
  public void snack(@NonNull String message) {
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void snackLong(@NonNull String message) {
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (Preferences.WEB_HEAD_ENABLED.equalsIgnoreCase(key)) {
      showHideErrorView();
    }
  }
}
