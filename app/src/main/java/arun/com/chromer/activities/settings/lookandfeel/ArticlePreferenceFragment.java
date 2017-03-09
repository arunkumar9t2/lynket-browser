package arun.com.chromer.activities.settings.lookandfeel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.activities.settings.widgets.IconListPreference;

import static arun.com.chromer.activities.settings.Preferences.ARTICLE_THEME;
import static arun.com.chromer.activities.settings.Preferences.WEB_HEAD_ENABLED;

public class ArticlePreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String[] SUMMARY_GROUP = new String[]{
            ARTICLE_THEME
    };
    private IconListPreference spawnLocation;

    public ArticlePreferenceFragment() {
        // Required empty public constructor
    }

    public static ArticlePreferenceFragment newInstance() {
        final ArticlePreferenceFragment fragment = new ArticlePreferenceFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.article_preferences);
        init();
        setIcons();
    }


    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceSummary(SUMMARY_GROUP);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceStates(key);
        updatePreferenceSummary(key);
    }

    private void init() {
        spawnLocation = (IconListPreference) findPreference(ARTICLE_THEME);
    }


    private void setIcons() {
        int materialLight = ContextCompat.getColor(getActivity(), R.color.material_dark_light);
        spawnLocation.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_format_color_fill)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
    }

    private void updatePreferenceStates(String key) {
        if (key.equalsIgnoreCase(WEB_HEAD_ENABLED)) {
            final boolean articleMode = Preferences.get(getContext()).articleMode();
            enableDisablePreference(articleMode, SUMMARY_GROUP);
        }
    }
}
