package arun.com.chromer.activities.settings.browsingoptions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.activities.settings.widgets.IconCheckboxPreference;
import arun.com.chromer.util.Utils;

import static arun.com.chromer.activities.settings.Preferences.AGGRESSIVE_LOADING;
import static arun.com.chromer.activities.settings.Preferences.MERGE_TABS_AND_APPS;
import static arun.com.chromer.activities.settings.Preferences.WEB_HEADS_COLOR;
import static arun.com.chromer.activities.settings.Preferences.WEB_HEAD_CLOSE_ON_OPEN;
import static arun.com.chromer.activities.settings.Preferences.WEB_HEAD_ENABLED;
import static arun.com.chromer.activities.settings.Preferences.WEB_HEAD_SIZE;
import static arun.com.chromer.activities.settings.Preferences.WEB_HEAD_SPAWN_LOCATION;

public class WebHeadOptionsFragment extends BasePreferenceFragment {

    private final String[] SUMMARY_GROUP = new String[]{
            WEB_HEAD_SPAWN_LOCATION,
            WEB_HEADS_COLOR,
            WEB_HEAD_SIZE,
    };

    private IconCheckboxPreference closeOnOpen;
    private IconCheckboxPreference aggressiveLoading;

    public WebHeadOptionsFragment() {
        // Required empty public constructor
    }

    public static WebHeadOptionsFragment newInstance() {
        final WebHeadOptionsFragment fragment = new WebHeadOptionsFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.webhead_options);
        init();
        setIcons();
        setupAggressivePreference();
    }


    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceStates(WEB_HEAD_ENABLED);
        updatePreferenceSummary(SUMMARY_GROUP);
        if (!Utils.isLollipopAbove()) {
            aggressiveLoading.setVisible(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceStates(key);
        updatePreferenceSummary(key);
        if (key.equalsIgnoreCase(MERGE_TABS_AND_APPS)) {
            if (!Preferences.get(getContext()).mergeTabs()) {
                aggressiveLoading.setChecked(false);
            }
        }
    }

    private void init() {
        closeOnOpen = (IconCheckboxPreference) findPreference(WEB_HEAD_CLOSE_ON_OPEN);
        aggressiveLoading = (IconCheckboxPreference) findPreference(AGGRESSIVE_LOADING);
    }


    private void setIcons() {
        final int materialLight = ContextCompat.getColor(getActivity(), R.color.material_dark_light);
        closeOnOpen.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_close_circle_outline)
                .color(materialLight)
                .sizeDp(24));
        aggressiveLoading.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_fast_forward)
                .color(materialLight)
                .sizeDp(24));
    }

    private void setupAggressivePreference() {
        aggressiveLoading.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (((Boolean) newValue) && !Preferences.get(getContext()).mergeTabs()) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.aggresive_dia_title)
                            .content(R.string.aggresive_dia_content)
                            .positiveText(android.R.string.ok)
                            .show();
                    Preferences.get(getContext()).mergeTabs(true);
                }
                return true;
            }
        });
    }

    private void updatePreferenceStates(String key) {
        if (key.equalsIgnoreCase(WEB_HEAD_ENABLED)) {
            final boolean webHeadsEnabled = Preferences.get(getContext()).webHeads();
            enableDisablePreference(webHeadsEnabled, WEB_HEAD_CLOSE_ON_OPEN, AGGRESSIVE_LOADING);
        }
    }
}
