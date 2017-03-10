package arun.com.chromer.activities.settings.browsingoptions;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.blacklist.BlacklistManagerActivity;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.activities.settings.widgets.IconSwitchPreference;
import arun.com.chromer.customtabs.warmup.WarmUpService;
import arun.com.chromer.util.Utils;

import static arun.com.chromer.activities.settings.Preferences.AGGRESSIVE_LOADING;
import static arun.com.chromer.activities.settings.Preferences.BLACKLIST_DUMMY;
import static arun.com.chromer.activities.settings.Preferences.MERGE_TABS_AND_APPS;

/**
 * Created by Arun on 21/06/2016.
 */
public class BehaviorPreferenceFragment extends BasePreferenceFragment {

    private IconSwitchPreference mergeTabsPreference;

    public BehaviorPreferenceFragment() {
        // Required empty public constructor
    }

    public static BehaviorPreferenceFragment newInstance() {
        final BehaviorPreferenceFragment fragment = new BehaviorPreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.behavior_preferences);
        // setup bottom bar preference
        setupMergeTabsPreference();
        setupBlacklistPreference();

        final IconSwitchPreference warmUpPreference = (IconSwitchPreference) findPreference(Preferences.WARM_UP);
        warmUpPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_lightbulb)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
    }

    private void setupBlacklistPreference() {
        final IconSwitchPreference blackListPreference = (IconSwitchPreference) findPreference(BLACKLIST_DUMMY);
        if (blackListPreference != null) {
            final Drawable recentImg = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_filter_variant)
                    .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                    .sizeDp(24);
            blackListPreference.setIcon(recentImg);
            blackListPreference.hideSwitch();
            blackListPreference.setOnPreferenceClickListener(preference -> {
                final Intent blacklistedApps = new Intent(getActivity(), BlacklistManagerActivity.class);
                startActivity(blacklistedApps,
                        ActivityOptions.makeCustomAnimation(getActivity(),
                                R.anim.slide_in_right_medium,
                                R.anim.slide_out_left_medium).toBundle()
                );
                return false;
            });
        }
    }

    private void setupMergeTabsPreference() {
        mergeTabsPreference = (IconSwitchPreference) findPreference(MERGE_TABS_AND_APPS);
        if (mergeTabsPreference != null) {
            final Drawable recentImg = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_animation)
                    .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                    .sizeDp(24);
            mergeTabsPreference.setIcon(recentImg);
            mergeTabsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!((Boolean) newValue) && Preferences.get(getContext()).aggressiveLoading()) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.merge_tabs_off_title)
                            .content(R.string.merget_tabs_off_content)
                            .positiveText(android.R.string.ok)
                            .show();
                    Preferences.get(getContext()).aggressiveLoading(false);
                }
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Utils.isLollipopAbove()) {
            mergeTabsPreference.setVisible(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase(AGGRESSIVE_LOADING)) {
            if (Preferences.get(getContext()).aggressiveLoading()) {
                mergeTabsPreference.setChecked(true);
            }
        }
        // Start stop warm up service
        if (key.equalsIgnoreCase(Preferences.WARM_UP)) {
            if (Preferences.get(getContext()).warmUp()) {
                getActivity().startService(new Intent(getActivity(), WarmUpService.class));
            } else {
                getActivity().stopService(new Intent(getActivity(), WarmUpService.class));
            }
        }
    }
}
