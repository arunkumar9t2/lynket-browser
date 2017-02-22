package arun.com.chromer.activities.settings.browsingmode;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.activities.settings.widgets.IconSwitchPreference;

public class BrowseFasterPreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public BrowseFasterPreferenceFragment() {
        // Required empty public constructor
    }

    public static BrowseFasterPreferenceFragment newInstance() {
        final BrowseFasterPreferenceFragment fragment = new BrowseFasterPreferenceFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.browse_faster_options);
        setupAmpPreference();
        setupArticlePreference();
    }

    private void setupArticlePreference() {
        final IconSwitchPreference articleModePreference = (IconSwitchPreference) findPreference(Preferences.ARTICLE_MODE);
        if (articleModePreference != null) {
            final Drawable articleImg = new IconicsDrawable(getActivity())
                    .icon(CommunityMaterial.Icon.cmd_file_image)
                    .color(ContextCompat.getColor(getActivity(), R.color.android_green))
                    .sizeDp(24);
            articleModePreference.setIcon(articleImg);
            articleModePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return false;
                }
            });
        }
    }

    private void setupAmpPreference() {
        final IconSwitchPreference ampModePreference = (IconSwitchPreference) findPreference(Preferences.AMP_MODE);
        if (ampModePreference != null) {
            ampModePreference.setIcon(R.drawable.ic_action_amp_icon);
            ampModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final boolean isSlideOver = !Preferences.get(getContext()).webHeads();
                    if (isSlideOver && (Boolean) newValue) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.amp_warning_title)
                                .content(R.string.amp_warning_content)
                                .positiveText(android.R.string.ok)
                                .iconRes(R.drawable.ic_action_amp_icon)
                                .show();
                    }
                    return true;
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
