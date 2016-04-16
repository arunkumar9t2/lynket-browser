package arun.com.chromer.preferences;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import arun.com.chromer.MainActivity;
import arun.com.chromer.R;
import arun.com.chromer.preferences.widgets.ColorPreference;
import arun.com.chromer.util.Constants;

public class WebHeadPreferenceFragment extends DividerLessPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String WEBHEADS_COLOR_KEY = "WEBHEADS_COLOR_KEY";

    public WebHeadPreferenceFragment() {
        // Required empty public constructor
    }

    private final BroadcastReceiver mColorSelectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int selectedColor = intent.getIntExtra(WEBHEADS_COLOR_KEY, 0);
            if (selectedColor != 0) {
                ColorPreference preference = (ColorPreference) findPreference(Preferences.WEBHEADS_COLOR);
                if (preference != null) {
                    preference.setColor(selectedColor);
                }
            }
        }
    };

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
        setupWebHeadColorPreference();
    }

    private void setupWebHeadColorPreference() {
        ColorPreference webheadsColorPref = (ColorPreference) findPreference(Preferences.WEBHEADS_COLOR);
        if (webheadsColorPref != null) {
            webheadsColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int chosenColor = ((ColorPreference) preference).getColor();
                    new ColorChooserDialog.Builder((MainActivity) getActivity(), R.string.web_heads_color)
                            .titleSub(R.string.web_heads_color)
                            .allowUserColorInputAlpha(false)
                            .preselect(chosenColor)
                            .dynamicButtonColor(false)
                            .show();
                    return true;
                }
            });
        }

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
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mColorSelectionReceiver, new IntentFilter(Constants.ACTION_TOOLBAR_COLOR_SET));
        updatePreferenceSummary();
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mColorSelectionReceiver);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceSummary();
    }

    private void updatePreferenceSummary() {
        boolean webHeadsEnabled = Preferences.webHeads(getActivity().getApplicationContext());

        ListPreference spawnPreference = (ListPreference) findPreference(Preferences.WEB_HEAD_SPAWN_LOCATION);
        if (spawnPreference != null) {
            spawnPreference.setSummary(spawnPreference.getEntry());
            spawnPreference.setEnabled(webHeadsEnabled);
        }

        ColorPreference webheadColorPref = (ColorPreference) findPreference(Preferences.WEBHEADS_COLOR);
        if (webheadColorPref != null) {
            webheadColorPref.refreshSummary();
            webheadColorPref.setEnabled(webHeadsEnabled);
        }

        CheckBoxPreference closeWebHeadsPref = (CheckBoxPreference) findPreference(Preferences.WEB_HEAD_CLOSE_ON_OPEN);
        if (closeWebHeadsPref != null) {
            closeWebHeadsPref.setEnabled(webHeadsEnabled);
        }
    }
}
