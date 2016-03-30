package arun.com.chromer.preferences;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;

public class WebHeadPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public WebHeadPreferenceFragment() {
        // Required empty public constructor
    }

    public static WebHeadPreferenceFragment newInstance() {
        WebHeadPreferenceFragment fragment = new WebHeadPreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.webhead_preferences);
        setUpWebHeadSwitch();
    }

    private void setUpWebHeadSwitch() {
        SwitchPreferenceCompat webHeadSwitch = (SwitchPreferenceCompat) findPreference(Preferences.WEB_HEAD_ENABLED);
        if (webHeadSwitch != null) {
            webHeadSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                    boolean isChecked = switchCompat.isChecked();
                    if (isChecked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.canDrawOverlays(getActivity())) {
                                // Don't check the switch until permission is granted
                                switchCompat.setChecked(false);
                                new MaterialDialog.Builder(getActivity())
                                        .title(R.string.permission_required)
                                        .content(R.string.overlay_permission_content)
                                        .positiveText(R.string.grant)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @TargetApi(Build.VERSION_CODES.M)
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();
                                                final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        Uri.parse("package:" + getActivity().getPackageName()));
                                                getActivity().startActivityForResult(intent, 0);
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreferenceSummary();
    }

    @Override
    public void onPause() {
        getPreferenceManager()
                .getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceSummary();
    }

    private void updatePreferenceSummary() {
        ListPreference preference = (ListPreference) findPreference(Preferences.WEB_HEAD_SPAWN_LOCATION);
        if (preference != null) {
            preference.setSummary(preference.getEntry());
        }
    }
}
