package arun.com.chromer.preferences;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by Arun on 02/03/2016.
 */
public class DividerLessPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // To be used by deriving classes
    }

    @Override
    public void setDivider(Drawable divider) {
        // Don't do anything here
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        recyclerView.setNestedScrollingEnabled(false);
        return recyclerView;
    }
}
