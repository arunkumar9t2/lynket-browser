package arun.com.chromer.activities.about.changelog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.R;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 25/12/2015.
 */
public class ChangelogUtil {
    public static void showChangelogDialog(final Activity activity) {
        try {
            // TODO Load data in background, show progress drawable till then
            final WebView webView = new WebView(activity.getApplication());
            webView.loadData(activity.getString(R.string.changelog_text), "text/html", "utf-8");

            new MaterialDialog.Builder(activity)
                    .customView(webView, true)
                    .title("Changelog")
                    .positiveText(android.R.string.ok)
                    .neutralText(R.string.rate_play_store)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Util.openPlayStore(activity, activity.getPackageName());
                        }
                    })
                    .show();
        } catch (Exception e) {
            Toast.makeText(activity, R.string.changelog_skipped, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean shouldShowChangelog(Context context) {
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(),
                Context.MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return false;
        } else if (savedVersionCode == DOESNT_EXIST) {
            // This is a new install (or the user cleared the shared preferences)
            return true;
        } else if (currentVersionCode > savedVersionCode) {
            // This is an upgrade
            return true;
        }
        return false;
    }
}
