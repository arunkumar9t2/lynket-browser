package arun.com.chromer.activities.settings.browsingoptions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.preferences.BasePreferenceFragment;

public class BottomBarPreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public BottomBarPreferenceFragment() {
        // Required empty public constructor
    }

    public static BottomBarPreferenceFragment newInstance() {
        final BottomBarPreferenceFragment fragment = new BottomBarPreferenceFragment();
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
        final SwitchPreferenceCompat bottomBarPreference = (SwitchPreferenceCompat) findPreference(Preferences.BOTTOM_BAR_ENABLED);
        bottomBarPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_more)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
