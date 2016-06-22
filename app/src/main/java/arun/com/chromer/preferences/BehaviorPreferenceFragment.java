package arun.com.chromer.preferences;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.blacklist.BlacklistManagerActivity;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.preferences.widgets.IconSwitchPreference;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 21/06/2016.
 */
public class BehaviorPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private IconSwitchPreference mBlackListPreference;
    private IconSwitchPreference mMergeTabsPreference;

    public BehaviorPreferenceFragment() {
        // Required empty public constructor
    }

    public static BehaviorPreferenceFragment newInstance() {
        BehaviorPreferenceFragment fragment = new BehaviorPreferenceFragment();
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
    }

    private void setupBlacklistPreference() {
        mBlackListPreference = (IconSwitchPreference) findPreference(Preferences.BLACKLIST_DUMMY);
        if (mBlackListPreference != null) {
            Drawable recentImg = new IconicsDrawable(getActivity())
                    .icon(GoogleMaterial.Icon.gmd_filter_list)
                    .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                    .sizeDp(24);
            mBlackListPreference.setIcon(recentImg);
            mBlackListPreference.hideSwitch();
            mBlackListPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent blacklistedApps = new Intent(getActivity(), BlacklistManagerActivity.class);
                    startActivity(blacklistedApps,
                            ActivityOptions.makeCustomAnimation(getActivity(),
                                    R.anim.slide_in_right_medium,
                                    R.anim.slide_out_left_medium).toBundle()
                    );
                    return false;
                }
            });
        }
    }

    private void setupMergeTabsPreference() {
        mMergeTabsPreference = (IconSwitchPreference) findPreference(Preferences.MERGE_TABS_AND_APPS);
        if (mMergeTabsPreference != null) {
            Drawable recentImg = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_animation)
                    .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                    .sizeDp(24);
            mMergeTabsPreference.setIcon(recentImg);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (!Util.isLollipopAbove()) {
            mMergeTabsPreference.setVisible(false);
        }
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
