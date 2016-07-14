package arun.com.chromer.preferences;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.MainActivity;
import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.preferences.widgets.ColorPreference;
import arun.com.chromer.preferences.widgets.IconListPreference;
import arun.com.chromer.preferences.widgets.IconSwitchPreference;
import arun.com.chromer.shared.AppDetectService;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Util;

public class PersonalizationPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String[] PREFERENCE_GROUP = new String[]{
            Preferences.ANIMATION_SPEED,
            Preferences.ANIMATION_TYPE,
            Preferences.PREFERRED_ACTION,
            Preferences.TOOLBAR_COLOR
    };

    private final IntentFilter mToolBarColorFilter = new IntentFilter(Constants.ACTION_TOOLBAR_COLOR_SET);

    private IconSwitchPreference mDynamicColor;
    private IconSwitchPreference mIsColoredToolbar;
    private ColorPreference mToolbarColorPref;
    private IconListPreference mAnimationSpeed;
    private IconListPreference mOpeningAnimation;
    private IconListPreference mPreferredAction;

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
        // init and set icon
        init();
        setupIcons();

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
        updatePreferenceStates(Preferences.ANIMATION_TYPE);
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

    private void init() {
        mDynamicColor = (IconSwitchPreference) findPreference(Preferences.DYNAMIC_COLOR);
        mIsColoredToolbar = (IconSwitchPreference) findPreference(Preferences.TOOLBAR_COLOR_PREF);
        mToolbarColorPref = (ColorPreference) findPreference(Preferences.TOOLBAR_COLOR);
        mPreferredAction = (IconListPreference) findPreference(Preferences.PREFERRED_ACTION);
        mOpeningAnimation = (IconListPreference) findPreference(Preferences.ANIMATION_TYPE);
        mAnimationSpeed = (IconListPreference) findPreference(Preferences.ANIMATION_SPEED);
    }

    private void setupIcons() {
        Drawable palette = new IconicsDrawable(getActivity())
                .icon(GoogleMaterial.Icon.gmd_color_lens)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24);
        mToolbarColorPref.setIcon(palette);
        mIsColoredToolbar.setIcon(palette);
        mDynamicColor.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_format_color_fill)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        mPreferredAction.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_heart)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        mOpeningAnimation.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_image_filter_none)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        mAnimationSpeed.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_speedometer)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
    }


    private void updatePreferenceStates(String key) {
        if (key.equalsIgnoreCase(Preferences.TOOLBAR_COLOR_PREF)) {
            final boolean webHeadsEnabled = Preferences.isColoredToolbar(getActivity());
            enableDisablePreference(webHeadsEnabled,
                    Preferences.TOOLBAR_COLOR,
                    Preferences.DYNAMIC_COLOR
            );
        } else if (key.equalsIgnoreCase(Preferences.ANIMATION_TYPE)) {
            final boolean animationEnabled = Preferences.isAnimationEnabled(getActivity());
            enableDisablePreference(animationEnabled, Preferences.ANIMATION_SPEED);
        }

        updateDynamicSummary();
    }

    private void updateDynamicSummary() {
        mDynamicColor.setSummary(Preferences.dynamicColorSummary(getActivity()));
        boolean isColoredToolbar = Preferences.isColoredToolbar(getActivity());
        if (!isColoredToolbar) {
            mDynamicColor.setChecked(false);
        }
    }

    private void setupDynamicToolbar() {
        mDynamicColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                final boolean isChecked = switchCompat.isChecked();
                if (isChecked) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.dynamic_toolbar_color)
                            .content(R.string.dynamic_toolbar_help)
                            .items(getString(R.string.based_on_app),
                                    getString(R.string.based_on_web))
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


    private void setupToolbarColorPreference() {
        mToolbarColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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

    private void requestUsagePermissionIfNeeded() {
        if (Preferences.dynamicToolbarOnApp(getActivity()) && !Util.canReadUsageStats(getActivity())) {
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
            getActivity().startService(new Intent(getActivity().getApplicationContext(), AppDetectService.class));
        else
            getActivity().stopService(new Intent(getActivity().getApplicationContext(), AppDetectService.class));
    }

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
}
