/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

package arun.com.chromer.activities.settings.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.widgets.IconSwitchPreference;
import arun.com.chromer.customtabs.warmup.WarmUpService;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Utils;

/**
 * Created by Arun on 19/06/2016.
 */
public class PrefetchPreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String[] PREFERENCE_GROUP = new String[]{
            Preferences.WIFI_PREFETCH,
            Preferences.PRE_FETCH_NOTIFICATION,
    };
    private IconSwitchPreference mWarmupPreference;
    private IconSwitchPreference mPrefetchPreference;

    public PrefetchPreferenceFragment() {
        // Required empty public constructor
    }

    public static PrefetchPreferenceFragment newInstance() {
        PrefetchPreferenceFragment fragment = new PrefetchPreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefetch_preferences);
        mPrefetchPreference = (IconSwitchPreference) findPreference(Preferences.PRE_FETCH);
        mWarmupPreference = (IconSwitchPreference) findPreference(Preferences.WARM_UP);
        mWarmupPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_lightbulb)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        mPrefetchPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_relative_scale)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));

        mPrefetchPreference.setOnPreferenceClickListener(preference -> {
            final boolean isChecked = mPrefetchPreference.isChecked();
            if (isChecked) {
                if (!Utils.isAccessibilityServiceEnabled(getActivity())) {
                    mPrefetchPreference.setChecked(false);
                    guideUserToAccessibilitySettings(true);
                    mWarmupPreference.setEnabled(true);
                    mWarmupPreference.setChecked(Preferences.get(getContext()).warmUp());
                } else {
                    mWarmupPreference.setEnabled(false);
                    mWarmupPreference.setChecked(true);
                }
            } else {
                mWarmupPreference.setChecked(Preferences.get(getContext()).warmUp());
                guideUserToAccessibilitySettings(false);
                mWarmupPreference.setEnabled(true);
            }
            ServiceUtil.takeCareOfServices(getActivity());
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean isPrefetchEnabled = isPrefetchEnabled();
        mPrefetchPreference.setChecked(isPrefetchEnabled);
        mWarmupPreference.setEnabled(!isPrefetchEnabled);
        enableDisablePreference(isPrefetchEnabled, PREFERENCE_GROUP);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        enableDisablePreference(isPrefetchEnabled(), PREFERENCE_GROUP);
        // Start stop warm up service
        if (key.equalsIgnoreCase(Preferences.WARM_UP)) {
            if (Preferences.get(getContext()).warmUp()) {
                getActivity().startService(new Intent(getActivity(), WarmUpService.class));
            } else {
                getActivity().stopService(new Intent(getActivity(), WarmUpService.class));
            }
        }
    }

    private boolean isPrefetchEnabled() {
        return Utils.isAccessibilityServiceEnabled(getActivity()) && Preferences.get(getContext()).preFetch();
    }

    private void guideUserToAccessibilitySettings(boolean prefetchEnabled) {
        if (prefetchEnabled) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.accessibility_dialog_title)
                    .content(R.string.accessibility_dialog_desc)
                    .positiveText(R.string.open_settings)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                    })
                    .show();
        } else {
            // Ask user to revoke accessibility permission
            Toast.makeText(getActivity(), R.string.revoke_accessibility_permission, Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
        }
    }
}
