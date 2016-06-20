package arun.com.chromer.preferences;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import arun.com.chromer.MainActivity;
import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.preferences.widgets.ColorPreference;
import arun.com.chromer.shared.AppDetectService;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Util;

public class PersonalizationPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final BroadcastReceiver mColorSelectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int selectedColor = intent.getIntExtra(Constants.EXTRA_KEY_TOOLBAR_COLOR, Constants.NO_COLOR);
            if (selectedColor != Constants.NO_COLOR) {
                ColorPreference preference = (ColorPreference) findPreference(Preferences.TOOLBAR_COLOR);
                if (preference != null) {
                    preference.setColor(selectedColor);
                }
            }
        }
    };
    private final String[] PREFERENCE_GROUP = new String[]{
            Preferences.ANIMATION_SPEED,
            Preferences.ANIMATION_TYPE,
            Preferences.PREFERRED_ACTION,
            Preferences.TOOLBAR_COLOR
    };
    private IntentFilter mToolBarColorFilter = new IntentFilter(Constants.ACTION_TOOLBAR_COLOR_SET);

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
        addPreferencesFromResource(R.xml.personalization_preferences);

        // setup preferences after creation
        setupToolbarColorPreference();
        setupDynamicToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mColorSelectionReceiver, mToolBarColorFilter);
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceStates(Preferences.TOOLBAR_COLOR_PREF);
        updatePreferenceSummary(PREFERENCE_GROUP);
    }

    @Override
    public void onPause() {
        unregisterReceiver(mColorSelectionReceiver);
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceStates(key);
        updatePreferenceSummary(PREFERENCE_GROUP);
    }

    private void updatePreferenceStates(String key) {
        if (key.equalsIgnoreCase(Preferences.TOOLBAR_COLOR_PREF)) {
            final boolean webHeadsEnabled = Preferences.isColoredToolbar(getActivity());
            enableDisablePreference(webHeadsEnabled,
                    Preferences.TOOLBAR_COLOR,
                    Preferences.DYNAMIC_COLOR
            );
        }

        updateDynamicSummary();
    }

    private void updateDynamicSummary() {
        final SwitchPreferenceCompat dynamicColor = (SwitchPreferenceCompat) findPreference(Preferences.DYNAMIC_COLOR);
        if (dynamicColor != null) {
            dynamicColor.setSummary(Preferences.dynamicColorSummary(getActivity()));
            boolean isColoredToolbar = Preferences.isColoredToolbar(getActivity());
            if (!isColoredToolbar) {
                dynamicColor.setChecked(false);
            }
        }
    }

    private void setupDynamicToolbar() {
        final SwitchPreferenceCompat switchPreferenceCompat = (SwitchPreferenceCompat) findPreference(Preferences.DYNAMIC_COLOR);
        if (switchPreferenceCompat != null) {
            switchPreferenceCompat.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                    final boolean isChecked = switchCompat.isChecked();
                    if (isChecked) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.dynamic_toolbar_color)
                                .content(R.string.dynamic_toolbar_help)
                                .items(new String[]{
                                        getString(R.string.based_on_app),
                                        getString(R.string.based_on_web)})
                                .positiveText(android.R.string.ok)
                                .alwaysCallMultiChoiceCallback()
                                .itemsCallbackMultiChoice(Preferences.dynamicToolbarSelections(getActivity()),
                                        new MaterialDialog.ListCallbackMultiChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog,
                                                                       Integer[] which,
                                                                       CharSequence[] text) {
                                                if (which.length == 0) {
                                                    switchCompat.setChecked(false);
                                                    Preferences.dynamicToolbar(getActivity(), false);
                                                } else switchCompat.setChecked(true);

                                                Preferences.updateAppAndWeb(getActivity(), which);
                                                requestUsagePermissionIfNeeded();
                                                handleAppDetectionService();
                                                updateDynamicSummary();
                                                return true;
                                            }
                                        })
                                .dismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        switchCompat.setChecked(Preferences.dynamicToolbar(getActivity()));
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


    private void setupToolbarColorPreference() {
        final ColorPreference toolbarColorPref = (ColorPreference) findPreference(Preferences.TOOLBAR_COLOR);
        if (toolbarColorPref != null) {
            toolbarColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int chosenColor = ((ColorPreference) preference).getColor();
                    new ColorChooserDialog.Builder((MainActivity) getActivity(), R.string.default_toolbar_color)
                            .titleSub(R.string.default_toolbar_color)
                            .allowUserColorInputAlpha(false)
                            .preselect(chosenColor)
                            .dynamicButtonColor(false)
                            .show();
                    return true;
                }
            });
        }
    }

    private void requestUsagePermissionIfNeeded() {
        if (Preferences.dynamicToolbarOnApp(getActivity())
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !Util.canReadUsageStats(getActivity())) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.permission_required)
                    .content(R.string.usage_permission_explanation_appcolor)
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
        if (ServiceUtil.isAppBasedToolbarColor(getActivity()) || Preferences.blacklist(getActivity()))
            getActivity().startService(new Intent(getActivity().getApplicationContext(),
                    AppDetectService.class));
        else
            getActivity().stopService(new Intent(getActivity().getApplicationContext(),
                    AppDetectService.class));
    }
}
