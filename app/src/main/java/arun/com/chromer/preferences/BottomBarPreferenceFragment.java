package arun.com.chromer.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;

public class BottomBarPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public BottomBarPreferenceFragment() {
        // Required empty public constructor
    }

    public static BottomBarPreferenceFragment newInstance() {
        BottomBarPreferenceFragment fragment = new BottomBarPreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bottombar_preferences);
        // setup bottom bar preference
        setupBottomBarPreference();
    }

    private void setupBottomBarPreference() {
        SwitchPreferenceCompat bottomBarSwitch = (SwitchPreferenceCompat) findPreference(Preferences.BOTTOM_BAR_ENABLED);
        if (bottomBarSwitch != null) {
            bottomBarSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                    boolean isChecked = switchCompat.isChecked();

                    if (isChecked && Preferences.dummyBottomBar(getActivity().getApplicationContext())) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.bottom_bar)
                                .content(R.string.bottom_bar_expln)
                                .positiveText(android.R.string.ok)
                                .show();
                    }
                    return false;
                }
            });
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
