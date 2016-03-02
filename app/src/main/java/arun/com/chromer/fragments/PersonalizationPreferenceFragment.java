package arun.com.chromer.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;
import arun.com.chromer.services.AppDetectService;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.ServicesUtil;
import arun.com.chromer.util.Util;

public class PersonalizationPreferenceFragment extends DividerLessPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public PersonalizationPreferenceFragment() {
        // Required empty public constructor
    }

    public static PersonalizationPreferenceFragment newInstance() {
        PersonalizationPreferenceFragment fragment = new PersonalizationPreferenceFragment();
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
                    final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                    boolean isChecked = switchCompat.isChecked();
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
                                                if (which.length == 0)
                                                    switchCompat.setChecked(false);
                                                else switchCompat.setChecked(true);

                                                Preferences.updateAppAndWeb(getActivity()
                                                        .getApplicationContext(), which);
                                                requestUsagePermissionIfNeeded();
                                                handleAppDetectionService();
                                                updateDynamicSummary();
                                                return true;
                                            }
                                        })
                                .show();
                        requestUsagePermissionIfNeeded();
                    }
                    handleAppDetectionService();
                    updateDynamicSummary();
                    return false;
                }
            });
        }
    }

    private void requestUsagePermissionIfNeeded() {
        if (Preferences.dynamicToolbarOnApp(getActivity().getApplicationContext())
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !Util.canReadUsageStats(getActivity().getApplicationContext())) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.permission_required)
                    .content(R.string.usage_permission_explanation)
                    .positiveText(R.string.grant)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO Some devices don't have this activity. Should // FIXME: 28/02/2016
                            getActivity().startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        }
                    }).show();
        }

    }

    private void handleAppDetectionService() {
        if (ServicesUtil.isAppBasedToolbarColor(getActivity()) || Preferences.blacklist(getActivity()))
            getActivity().startService(new Intent(getActivity().getApplicationContext(),
                    AppDetectService.class));
        else
            getActivity().stopService(new Intent(getActivity().getApplicationContext(),
                    AppDetectService.class));
    }
}
