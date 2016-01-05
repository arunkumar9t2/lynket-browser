package arun.com.chromer.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import arun.com.chromer.R;

public class PreferenceFragment extends PreferenceFragmentCompat {

    public PreferenceFragment() {
        // Required empty public constructor
    }

    public static PreferenceFragment newInstance() {
        PreferenceFragment fragment = new PreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

}
