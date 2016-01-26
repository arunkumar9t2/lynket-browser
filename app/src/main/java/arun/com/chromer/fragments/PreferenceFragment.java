package arun.com.chromer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

import arun.com.chromer.R;
import arun.com.chromer.util.Preferences;

public class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public PreferenceFragment() {
        // Required empty public constructor
    }

    public static PreferenceFragment newInstance() {
        PreferenceFragment fragment = new PreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen()
                .getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceSummary();
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private void updatePreferenceSummary() {
        // Showing summary for animation preference
        ListPreference preference = (ListPreference) findPreference(Preferences.ANIMATION_TYPE);
        if (preference != null) {
            preference.setSummary(preference.getEntry());
        }
        // Showing summary for preferred action
        preference = (ListPreference) findPreference(Preferences.PREFERRED_ACTION);
        if (preference != null) {
            preference.setSummary(preference.getEntry());
        }
    }
}
