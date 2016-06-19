package arun.com.chromer.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import arun.com.chromer.R;

/**
 * Created by Arun on 19/06/2016.
 */
public class PrefetchPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public PrefetchPreferenceFragment() {
        // Required empty public constructor
    }

    public static PrefetchPreferenceFragment newInstance() {
        PrefetchPreferenceFragment fragment = new PrefetchPreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefetch_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
