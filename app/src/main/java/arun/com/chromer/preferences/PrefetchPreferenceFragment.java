package arun.com.chromer.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 19/06/2016.
 */
public class PrefetchPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String[] PREFERENCE_GROUP = new String[]{
            Preferences.WIFI_PREFETCH,
            Preferences.PRE_FETCH_NOTIFICATION,
    };
    SwitchPreferenceCompat mWarmupPreference;
    SwitchPreferenceCompat mPrefetchPreference;

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
        mPrefetchPreference = (SwitchPreferenceCompat) findPreference(Preferences.PRE_FETCH);
        mWarmupPreference = (SwitchPreferenceCompat) findPreference(Preferences.WARM_UP);

        mPrefetchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final boolean isChecked = mPrefetchPreference.isChecked();
                if (isChecked) {
                    if (!Util.isAccessibilityServiceEnabled(getActivity())) {
                        mPrefetchPreference.setChecked(false);
                        guideUserToAccessibilitySettings(true);
                        mWarmupPreference.setEnabled(true);
                        mWarmupPreference.setChecked(Preferences.warmUp(getActivity()));
                    } else {
                        mWarmupPreference.setEnabled(false);
                        mWarmupPreference.setChecked(true);
                    }
                } else {
                    mWarmupPreference.setChecked(Preferences.warmUp(getActivity()));
                    guideUserToAccessibilitySettings(false);
                    mWarmupPreference.setEnabled(true);
                }
                ServiceUtil.takeCareOfServices(getActivity());
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        ServiceUtil.takeCareOfServices(getActivity());
        final boolean isPrefetchEnabled = isPrefetchEnabled();
        mPrefetchPreference.setChecked(isPrefetchEnabled);
        mWarmupPreference.setEnabled(!isPrefetchEnabled);
        enableDisablePreference(isPrefetchEnabled(), PREFERENCE_GROUP);
    }

    @Override
    public void onPause() {
        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        enableDisablePreference(isPrefetchEnabled(), PREFERENCE_GROUP);
        ServiceUtil.takeCareOfServices(getActivity());
    }

    public boolean isPrefetchEnabled() {
        return Util.isAccessibilityServiceEnabled(getActivity()) && Preferences.preFetch(getActivity());
    }

    private void guideUserToAccessibilitySettings(boolean prefetchEnabled) {
        if (prefetchEnabled) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.accessibility_dialog_title)
                    .content(R.string.accessibility_dialog_desc)
                    .positiveText(R.string.open_settings)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                        }
                    })
                    .show();
        } else {
            // Ask user to revoke accessibility permission
            Toast.makeText(getActivity(), R.string.revoke_accessibility_permission, Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
        }
    }
}
