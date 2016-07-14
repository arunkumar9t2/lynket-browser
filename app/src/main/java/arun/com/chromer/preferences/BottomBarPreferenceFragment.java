package arun.com.chromer.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;

public class BottomBarPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SwitchPreferenceCompat mBottomBarPreference;

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
        mBottomBarPreference = (SwitchPreferenceCompat) findPreference(Preferences.BOTTOM_BAR_ENABLED);
        mBottomBarPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(GoogleMaterial.Icon.gmd_space_bar)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        if (mBottomBarPreference != null) {
            mBottomBarPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
