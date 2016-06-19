package arun.com.chromer.preferences;

import android.os.Bundle;

import arun.com.chromer.R;

/**
 * Created by Arun on 19/06/2016.
 */
public class PrefetchPreferenceFragment extends DividerLessPreferenceFragment {
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
}
