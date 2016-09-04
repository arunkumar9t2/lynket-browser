package arun.com.chromer.preferences;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import arun.com.chromer.MainActivity;
import arun.com.chromer.preferences.widgets.ColorPreference;
import timber.log.Timber;

/**
 * Created by Arun on 02/03/2016.
 */
public abstract class DividerLessPreferenceFragment extends PreferenceFragmentCompat {
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean mDebug = false;

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

    void enableDisablePreference(boolean enabled, String... preferenceKeys) {
        for (String preferenceKey : preferenceKeys) {
            final Preference preference = findPreference(preferenceKey);
            if (preference != null) {
                preference.setEnabled(enabled);
            }
        }
    }

    void updatePreferenceSummary(String... preferenceKeys) {
        for (String key : preferenceKeys) {
            final Preference preference = getPreferenceScreen().findPreference(key);
            if (preference instanceof ListPreference) {
                final ListPreference listPreference = (ListPreference) preference;
                listPreference.setSummary(listPreference.getEntry());
                debug("Set %s preference to %s", listPreference.getTitle(), listPreference.getEntry());
            } else if (preference instanceof ColorPreference) {
                final ColorPreference colorPreference = (ColorPreference) preference;
                colorPreference.refreshSummary();
            }
        }
    }

    private void debug(String string, Object... args) {
        //noinspection ConstantConditions
        if (mDebug) {
            Timber.d(string, args);
        }
    }

    @NonNull
    protected LocalBroadcastManager getLocalBroadcastManager() {
        return LocalBroadcastManager.getInstance(getActivity());
    }

    void registerReceiver(@Nullable BroadcastReceiver receiver, @Nullable IntentFilter filter) {
        if (receiver != null && filter != null) {
            getLocalBroadcastManager().registerReceiver(receiver, filter);
        }
    }

    void unregisterReceiver(@Nullable BroadcastReceiver receiver) {
        if (receiver != null) {
            getLocalBroadcastManager().unregisterReceiver(receiver);
        }
    }

    /**
     * Shows a {@link android.support.design.widget.Snackbar} by calling activity methods.
     */
    void showSnack(@NonNull String textToSnack, int duration) {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.snack(textToSnack, duration);
        }
    }

    @NonNull
    SharedPreferences getSharedPreferences() {
        return getPreferenceManager().getSharedPreferences();
    }
}
