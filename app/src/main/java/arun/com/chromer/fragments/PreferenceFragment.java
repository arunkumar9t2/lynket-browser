package arun.com.chromer.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.AppDetectService;
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
        configureDynamicToolbar();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceSummary();
    }

    @Override
    public void onPause() {
        getPreferenceManager()
                .getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceSummary();
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

        updateDynamicSummary();
    }

    private void updateDynamicSummary() {
        SwitchPreferenceCompat dynamicColor = (SwitchPreferenceCompat) findPreference(Preferences.DYNAMIC_COLOR);
        if (dynamicColor != null) {
            dynamicColor.setSummary(Preferences.dynamicColorSummary(getActivity().getApplicationContext()));
        }
    }

    private void configureDynamicToolbar() {
        SwitchPreferenceCompat switchPreferenceCompat = (SwitchPreferenceCompat) findPreference(Preferences.DYNAMIC_COLOR);
        if (switchPreferenceCompat != null) {
            switchPreferenceCompat.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SwitchPreferenceCompat switc = (SwitchPreferenceCompat) preference;
                    boolean isChecked = switc.isChecked();
                    if (isChecked) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.dynamic_toolbar_color)
                                .content(R.string.dynamic_toolbar_help)
                                .items(new String[]{
                                        getString(R.string.based_on_app),
                                        getString(R.string.based_on_web)})
                                .positiveText(android.R.string.ok)
                                .alwaysCallMultiChoiceCallback()
                                .itemsCallbackMultiChoice(Preferences.dynamicToolbarSelections(getActivity().getApplicationContext()),
                                        new MaterialDialog.ListCallbackMultiChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog,
                                                                       Integer[] which,
                                                                       CharSequence[] text) {
                                                Preferences.updateAppAndWeb(getActivity()
                                                        .getApplicationContext(), which);
                                                startStopAppDetectionSrvc();
                                                updateDynamicSummary();
                                                return true;
                                            }
                                        })
                                .show();
                    }
                    updateDynamicSummary();
                    return false;
                }
            });
        }
    }

    private void startStopAppDetectionSrvc() {
        if (Preferences.dynamicToolbarOnApp(getActivity().getApplicationContext())
                && Preferences.dynamicToolbar(getActivity().getApplicationContext()))
            getActivity().startService(new Intent(getActivity().getApplicationContext(),
                    AppDetectService.class));
        else
            getActivity().stopService(new Intent(getActivity().getApplicationContext(),
                    AppDetectService.class));
    }
}
