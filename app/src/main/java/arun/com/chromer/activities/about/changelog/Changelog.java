package arun.com.chromer.activities.about.changelog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.R;
import arun.com.chromer.util.Utils;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by Arun on 25/12/2015.
 */
public class Changelog {
    public static void show(final Activity activity) {
        try {
            @SuppressLint("InflateParams")
            final FrameLayout content = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.widget_changelog_layout, null);
            final MaterialProgressBar progress = ButterKnife.findById(content, R.id.changelog_progress);
            final WebView webView = ButterKnife.findById(content, R.id.changelog_web_view);
            webView.loadData(activity.getString(R.string.changelog_text), "text/html", "utf-8");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    content.removeView(progress);
                    webView.setVisibility(View.VISIBLE);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return super.shouldOverrideUrlLoading(view, request);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.equalsIgnoreCase("https://goo.gl/photos/BzRV69ABov9zJxVu9")) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        activity.startActivity(i);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }
            });
            new MaterialDialog.Builder(activity)
                    .customView(content, false)
                    .title("Changelog")
                    .positiveText(android.R.string.ok)
                    .neutralText(R.string.rate_play_store)
                    .onNeutral((dialog, which) -> Utils.openPlayStore(activity, activity.getPackageName()))
                    .dismissListener(dialogInterface -> content.removeAllViews())
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, R.string.changelog_skipped, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Shows the changelog dialog if necessary
     *
     * @param activity Activty for which it should be shown
     */
    public static void conditionalShow(@NonNull final Activity activity) {
        if (shouldShow(activity)) {
            show(activity);
        }
    }

    private static boolean shouldShow(@NonNull final Context context) {
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOES_NT_EXIST = -1;
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;
        final SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(),
                Context.MODE_PRIVATE);

        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOES_NT_EXIST);
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();

        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return false;
        } else if (savedVersionCode == DOES_NT_EXIST) {
            // This is a new install (or the user cleared the shared preferences)
            return true;
        } else if (currentVersionCode > savedVersionCode) {
            // This is an upgrade
            return true;
        }
        return false;
    }
}
