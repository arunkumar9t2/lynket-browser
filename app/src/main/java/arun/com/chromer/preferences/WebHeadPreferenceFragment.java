package arun.com.chromer.preferences;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.MainActivity;
import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.preferences.widgets.ColorPreference;
import arun.com.chromer.preferences.widgets.IconCheckboxPreference;
import arun.com.chromer.preferences.widgets.IconListPreference;
import arun.com.chromer.preferences.widgets.IconSwitchPreference;
import arun.com.chromer.shared.Constants;

import static arun.com.chromer.preferences.manager.Preferences.AGGRESSIVE_LOADING;

public class WebHeadPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String[] WEBHEAD_PREFERENCE_GROUP = new String[]{
            Preferences.WEB_HEAD_SPAWN_LOCATION,
            Preferences.WEB_HEADS_COLOR,
            Preferences.WEB_HEAD_SIZE,
    };

    private final IntentFilter mWebHeadColorFilter = new IntentFilter(Constants.ACTION_WEBHEAD_COLOR_SET);

    private ColorPreference mWebHeadsColor;
    private IconSwitchPreference mWebHeadSwitch;
    private IconListPreference mSpawnLocation;
    private IconCheckboxPreference mFavicons;
    private IconCheckboxPreference mCloseOnOpen;
    private IconCheckboxPreference mAggressive;
    private IconListPreference mSize;

    public WebHeadPreferenceFragment() {
        // Required empty public constructor
    }

    public static WebHeadPreferenceFragment newInstance() {
        WebHeadPreferenceFragment fragment = new WebHeadPreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.webhead_preferences);
        init();
        setIcons();
        setUpWebHeadSwitch();
        setupWebHeadColorPreference();
    }


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mColorSelectionReceiver, mWebHeadColorFilter);
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceStates(Preferences.WEB_HEAD_ENABLED);
        updatePreferenceSummary(WEBHEAD_PREFERENCE_GROUP);
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
        updatePreferenceSummary(WEBHEAD_PREFERENCE_GROUP);
    }

    private void init() {
        mWebHeadSwitch = (IconSwitchPreference) findPreference(Preferences.WEB_HEAD_ENABLED);
        mWebHeadsColor = (ColorPreference) findPreference(Preferences.WEB_HEADS_COLOR);
        mSpawnLocation = (IconListPreference) findPreference(Preferences.WEB_HEAD_SPAWN_LOCATION);
        mSize = (IconListPreference) findPreference(Preferences.WEB_HEAD_SIZE);
        mFavicons = (IconCheckboxPreference) findPreference(Preferences.WEB_HEAD_FAVICON);
        mCloseOnOpen = (IconCheckboxPreference) findPreference(Preferences.WEB_HEAD_CLOSE_ON_OPEN);
        mAggressive = (IconCheckboxPreference) findPreference(AGGRESSIVE_LOADING);
    }


    private void setIcons() {
        int materialLight = ContextCompat.getColor(getActivity(), R.color.material_dark_light);
        mWebHeadsColor.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_palette)
                .color(materialLight)
                .sizeDp(24));
        mWebHeadSwitch.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_chart_bubble)
                .color(materialLight)
                .sizeDp(24));
        mSpawnLocation.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_code_tags)
                .color(materialLight)
                .sizeDp(24));
        mSize.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_crop_free)
                .color(materialLight)
                .sizeDp(24));
        mFavicons.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_image_filter_vintage)
                .color(materialLight)
                .sizeDp(24));
        mCloseOnOpen.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_close_circle_outline)
                .color(materialLight)
                .sizeDp(24));
        mAggressive.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_fast_forward)
                .color(materialLight)
                .sizeDp(24));
    }

    private void setupWebHeadColorPreference() {
        mWebHeadsColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int chosenColor = ((ColorPreference) preference).getColor();
                new ColorChooserDialog.Builder((MainActivity) getActivity(), R.string.web_heads_color)
                        .titleSub(R.string.web_heads_color)
                        .allowUserColorInputAlpha(false)
                        .preselect(chosenColor)
                        .dynamicButtonColor(false)
                        .show();
                return true;
            }
        });
    }

    private void setUpWebHeadSwitch() {
        mWebHeadSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                final boolean isChecked = switchCompat.isChecked();
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(getActivity())) {
                            // Don't check the switch until permission is granted
                            switchCompat.setChecked(false);
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.permission_required)
                                    .content(R.string.overlay_permission_content)
                                    .positiveText(R.string.grant)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @TargetApi(Build.VERSION_CODES.M)
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:" + getActivity().getPackageName()));
                                            getActivity().startActivityForResult(intent, 0);
                                        }
                                    })
                                    .show();
                        } else {
                            showSnack(getString(R.string.web_heads_enabled), Snackbar.LENGTH_LONG);
                        }
                    }
                }
                return false;
            }
        });
    }

    private void updatePreferenceStates(String key) {
        if (key.equalsIgnoreCase(Preferences.WEB_HEAD_ENABLED)) {
            final boolean webHeadsEnabled = Preferences.webHeads(getActivity());
            enableDisablePreference(webHeadsEnabled,
                    Preferences.WEB_HEAD_SPAWN_LOCATION,
                    Preferences.WEB_HEADS_COLOR,
                    Preferences.WEB_HEAD_CLOSE_ON_OPEN,
                    Preferences.WEB_HEAD_FAVICON,
                    Preferences.WEB_HEAD_SIZE,
                    AGGRESSIVE_LOADING
            );
        }
    }

    private final BroadcastReceiver mColorSelectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int selectedColor = intent.getIntExtra(Constants.EXTRA_KEY_WEBHEAD_COLOR, 0);
            if (selectedColor != 0) {
                final ColorPreference preference = (ColorPreference) findPreference(Preferences.WEB_HEADS_COLOR);
                if (preference != null) {
                    preference.setColor(selectedColor);
                }
            }
        }
    };
}
