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

package arun.com.chromer.settings.preferences;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import arun.com.chromer.settings.widgets.ColorPreference;
import arun.com.chromer.shared.base.Snackable;

/**
 * Created by Arun on 02/03/2016.
 */
public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    // To be used by deriving classes
  }

  @Override
  public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
    final RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
    // Needed for not eating touch event when flinged.
    recyclerView.setNestedScrollingEnabled(false);
    return recyclerView;
  }

  protected void enableDisablePreference(boolean enabled, String... preferenceKeys) {
    for (final String preferenceKey : preferenceKeys) {
      final Preference preference = findPreference(preferenceKey);
      if (preference != null) {
        preference.setEnabled(enabled);
      }
    }
  }

  protected void updatePreferenceSummary(String... preferenceKeys) {
    for (final String key : preferenceKeys) {
      final Preference preference = getPreferenceScreen().findPreference(key);
      if (preference instanceof ListPreference) {
        final ListPreference listPreference = (ListPreference) preference;
        listPreference.setSummary(listPreference.getEntry());
      } else if (preference instanceof ColorPreference) {
        final ColorPreference colorPreference = (ColorPreference) preference;
        colorPreference.refreshSummary();
      }
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
  public abstract void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key);

  protected void registerReceiver(@Nullable BroadcastReceiver receiver, @Nullable IntentFilter filter) {
    if (receiver != null && filter != null) {
      getLocalBroadcastManager().registerReceiver(receiver, filter);
    }
  }

  protected void unregisterReceiver(@Nullable BroadcastReceiver receiver) {
    if (receiver != null) {
      getLocalBroadcastManager().unregisterReceiver(receiver);
    }
  }

  @NonNull
  protected LocalBroadcastManager getLocalBroadcastManager() {
    return LocalBroadcastManager.getInstance(getActivity());
  }

  protected void snackLong(@NonNull final String textToSnack) {
    if (getActivity() instanceof Snackable) {
      final Snackable snackable = (Snackable) getActivity();
      snackable.snackLong(textToSnack);
    }
  }

  @NonNull
  protected SharedPreferences getSharedPreferences() {
    return getPreferenceManager().getSharedPreferences();
  }
}
