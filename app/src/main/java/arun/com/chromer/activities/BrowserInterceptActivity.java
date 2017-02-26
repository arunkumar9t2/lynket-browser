package arun.com.chromer.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.apps.AppRepository;
import arun.com.chromer.data.website.WebsiteRepository;
import arun.com.chromer.shared.AppDetectionManager;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.util.RxUtils;
import arun.com.chromer.util.SafeIntent;
import arun.com.chromer.util.Utils;
import arun.com.chromer.webheads.ui.ProxyActivity;
import timber.log.Timber;
import xyz.klinker.android.article.ArticleIntent;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_NEW_TAB;

@SuppressLint("GoogleAppIndexingApiWarning")
public class BrowserInterceptActivity extends AppCompatActivity {
    private MaterialDialog dialog;
    private SafeIntent safeIntent;
    private boolean isFromNewTab;

    @TargetApi(LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DocumentUtils.closeRootActivity(this);

        safeIntent = new SafeIntent(getIntent());
        if (safeIntent.getData() == null) {
            invalidLink();
            return;
        }
        isFromNewTab = safeIntent.getBooleanExtra(EXTRA_KEY_FROM_NEW_TAB, false);

        // Check if we should blacklist the launching app
        if (Preferences.get(this).blacklist()) {
            final String lastAppPackage = AppDetectionManager.getInstance(this).getNonFilteredPackage();
            if (!TextUtils.isEmpty(lastAppPackage)
                    && AppRepository.getInstance(this).isPackageBlacklisted(lastAppPackage)) {
                // The calling app was blacklisted by user, perform blacklisting.
                performBlacklistAction();
                return;
            }
        }

        // If user prefers to open in bubbles, then start the web head service.
        if (Preferences.get(this).webHeads()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, getString(R.string.web_head_permission_toast), LENGTH_LONG).show();
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    launchWebHead();
                }
            } else {
                launchWebHead();
            }
        } else if (Preferences.get(this).ampMode()) {
            closeDialogs();
            dialog = new MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .content(R.string.grabbing_amp_link)
                    .dismissListener(d -> finish())
                    .show();
            WebsiteRepository.getInstance(this)
                    .getWebsite(safeIntent.getData().toString())
                    .compose(RxUtils.applySchedulers())
                    .doOnNext(webSite -> {
                        if (webSite != null && !TextUtils.isEmpty(webSite.ampUrl)) {
                            dialog.setContent(R.string.link_found);
                            new Handler().postDelayed(() -> {
                                dialog.dismiss();
                                launchCCT(Uri.parse(webSite.ampUrl));
                            }, 100);
                        } else {
                            launchCCT(safeIntent.getData());
                        }
                    })
                    .doOnError(throwable -> {
                        Timber.e(throwable);
                        launchCCT(safeIntent.getData());
                        dialog.dismiss();
                    }).subscribe();
        } else if (Preferences.get(this).articleMode()) {
            final ArticleIntent intent = new ArticleIntent.Builder(this)
                    .setToolbarColor(Color.parseColor("#00BCD4"))
                    .setAccentColor(Color.parseColor("#EEFF41"))
                    .setTheme(ArticleIntent.THEME_DARK)
                    .setTextSize(15)     // 15 SP (default)
                    .build();
            intent.launchUrl(this, safeIntent.getData());
            finish();
        } else {
            launchCCT(safeIntent.getData());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void launchCCT(Uri uri) {
        closeDialogs();
        final Intent customTabActivity = new Intent(this, CustomTabActivity.class);
        customTabActivity.setData(uri);
        if (isFromNewTab || Preferences.get(this).mergeTabs()) {
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            customTabActivity.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        customTabActivity.putExtra(EXTRA_KEY_FROM_NEW_TAB, isFromNewTab);
        startActivity(customTabActivity);
        finish();
    }

    private void invalidLink() {
        Toast.makeText(this, getString(R.string.unsupported_link), LENGTH_SHORT).show();
        finish();
    }

    /**
     * Performs the blacklist action, which is opening the link we received in the user's
     * preferred browser.
     * We try to formulate a intent with user's secondary browser and launch it. If it fails we show
     * a dialog and explain what went wrong.
     */
    private void performBlacklistAction() {
        final String secondaryBrowserPackage = Preferences.get(this).secondaryBrowserPackage();
        if (secondaryBrowserPackage == null) {
            showSecondaryBrowserHandlingError(R.string.secondary_browser_not_error);
            return;
        }
        if (Utils.isPackageInstalled(this, secondaryBrowserPackage)) {
            final Intent webIntentExplicit = getOriginalIntentCopy(getIntent());
            webIntentExplicit.setPackage(secondaryBrowserPackage);
            try {
                startActivity(webIntentExplicit);
                finish();
            } catch (Exception e) {
                Timber.e("Secondary browser launch error: %s", e.getMessage());
                showSecondaryBrowserHandlingError(R.string.secondary_browser_launch_error);
            }
        } else {
            showSecondaryBrowserHandlingError(R.string.secondary_browser_not_installed);
        }
        finish();
    }

    private void showSecondaryBrowserHandlingError(@StringRes int stringRes) {
        closeDialogs();
        dialog = new MaterialDialog.Builder(this)
                .title(R.string.secondary_browser_launching_error_title)
                .content(stringRes)
                .iconRes(R.mipmap.ic_launcher)
                .positiveText(R.string.launch_setting)
                .negativeText(android.R.string.cancel)
                .theme(Theme.LIGHT)
                .positiveColorRes(R.color.colorAccent)
                .negativeColorRes(R.color.colorAccent)
                .onPositive((dialog1, which) -> {
                    final Intent chromerIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    chromerIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(chromerIntent);
                })
                .dismissListener(dialog12 -> finish()).show();
    }

    private void closeDialogs() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void launchWebHead() {
        final Intent webHeadLauncher = new Intent(this, ProxyActivity.class);
        webHeadLauncher.addFlags(FLAG_ACTIVITY_NEW_TASK);
        if (!isFromNewTab) {
            webHeadLauncher.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
        }
        webHeadLauncher.putExtra(EXTRA_KEY_FROM_NEW_TAB, isFromNewTab);
        webHeadLauncher.setData(safeIntent.getData());
        startActivity(webHeadLauncher);
        finish();
    }

    @NonNull
    private Intent getOriginalIntentCopy(@NonNull Intent originalIntent) {
        final Intent copy = new Intent(ACTION_VIEW, safeIntent.getData());
        if (originalIntent.getExtras() != null) {
            copy.putExtras(originalIntent.getExtras());
        }
        return copy;
    }
}
