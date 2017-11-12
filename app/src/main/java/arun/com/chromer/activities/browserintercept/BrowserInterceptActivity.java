/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.activities.browserintercept;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.activities.browsing.article.ArticleLauncher;
import arun.com.chromer.activities.common.BaseMVPActivity;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.apps.AppRepository;
import arun.com.chromer.data.website.WebsiteRepository;
import arun.com.chromer.di.components.ActivityComponent;
import arun.com.chromer.shared.AppDetectionManager;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.util.SafeIntent;
import arun.com.chromer.util.Utils;
import arun.com.chromer.webheads.ui.ProxyActivity;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.text.TextUtils.isEmpty;
import static android.widget.Toast.LENGTH_SHORT;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_NEW_TAB;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_OUR_APP;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_SKIP_EXTRACTION;
import static arun.com.chromer.util.RxUtils.applySchedulers;

@SuppressLint("GoogleAppIndexingApiWarning")
public class BrowserInterceptActivity extends BaseMVPActivity<BrowserIntercept.View, BrowserIntercept.Presenter> implements BrowserIntercept.View {
    private MaterialDialog dialog;
    private SafeIntent safeIntent;
    private boolean isFromNewTab;
    private boolean isFromOurApp;
    private boolean skipExtraction;

    private final CompositeSubscription subs = new CompositeSubscription();

    @Inject
    AppDetectionManager appDetectionManager;
    @Inject
    BrowserIntercept.Presenter presenter;


    // TODO Belongs in presenter.
    @Inject
    AppRepository appRepository;
    @Inject
    WebsiteRepository websiteRepository;

    @TargetApi(LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        safeIntent = new SafeIntent(getIntent());
        if (safeIntent.getData() == null) {
            invalidLink();
            return;
        }

        isFromNewTab = safeIntent.getBooleanExtra(EXTRA_KEY_FROM_NEW_TAB, false);
        isFromOurApp = safeIntent.getBooleanExtra(EXTRA_KEY_FROM_OUR_APP, false);
        skipExtraction = safeIntent.getBooleanExtra(EXTRA_KEY_SKIP_EXTRACTION, false);

        if (!isFromOurApp) {
            DocumentUtils.closeRootActivity(this);
        }

        // TODO All the code below belong in a presenter with a unit test :(

        // Check if we should blacklist the launching app
        if (Preferences.get(this).blacklist()) {
            final String lastAppPackage = appDetectionManager.getNonFilteredPackage();
            if (!isEmpty(lastAppPackage) && appRepository.isPackageBlacklisted(lastAppPackage)) {
                // The calling app was blacklisted by user, perform blacklisting.
                performBlacklistAction();
                return;
            }
        }

        // If user prefers to open in bubbles, then start the web head service.
        if (Preferences.get(this).webHeads()) {
            if (Utils.isOverlayGranted(this)) {
                launchWebHead();
            } else {
                Utils.openDrawOverlaySettings(this);
                finish();
            }
        } else if (Preferences.get(this).ampMode()) {
            closeDialogs();
            dialog = new MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .content(R.string.grabbing_amp_link)
                    .dismissListener(d -> finish())
                    .show();

            final Subscription subscription = websiteRepository
                    .getWebsite(safeIntent.getData().toString())
                    .compose(applySchedulers())
                    .doOnNext(webSite -> {
                        if (webSite != null && !TextUtils.isEmpty(webSite.ampUrl)) {
                            dialog.setContent(R.string.link_found);
                            new Handler().postDelayed(() -> {
                                dialog.dismiss();
                                // Got the AMP version, lets load that in CCT.
                                launchCCT(Uri.parse(webSite.ampUrl));
                            }, 100);
                        } else {
                            // AMP failed, try article if user prefers it and launch it.
                            dialog.setContent(R.string.link_not_found);
                            articleAwareLaunch();
                        }
                    })
                    .doOnError(throwable -> {
                        Timber.e(throwable);
                        articleAwareLaunch();
                        dialog.dismiss();
                    }).subscribe();
            subs.add(subscription);
        } else if (Preferences.get(this).articleMode()) {
            // User just wants article, load it.
            launchArticle();
        } else {
            launchCCT(safeIntent.getData());
        }
    }

    @Override
    protected void inject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected int getLayoutRes() {
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subs.clear();
    }

    @NonNull
    @Override
    public BrowserIntercept.Presenter createPresenter() {
        return presenter;
    }

    private void articleAwareLaunch() {
        if (Preferences.get(this).articleMode()) {
            launchArticle();
        } else {
            launchCCT(safeIntent.getData());
        }
    }

    private void launchArticle() {
        ArticleLauncher.from(this, safeIntent.getData())
                .applyCustomizations()
                .forNewTab(isFromNewTab)
                .launch();
        finish();
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
        customTabActivity.putExtra(EXTRA_KEY_SKIP_EXTRACTION, skipExtraction);
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
                .onPositive((dialog, which) -> {
                    final Intent chromerIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    chromerIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(chromerIntent);
                })
                .dismissListener(dialog -> finish()).show();
    }

    private void closeDialogs() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void launchWebHead() {
        final Intent webHeadLauncher = new Intent(this, ProxyActivity.class);
        webHeadLauncher.addFlags(FLAG_ACTIVITY_NEW_TASK);
        if (!isFromNewTab && !isFromOurApp) {
            webHeadLauncher.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
        }
        webHeadLauncher.putExtra(EXTRA_KEY_FROM_NEW_TAB, isFromNewTab);
        webHeadLauncher.putExtra(EXTRA_KEY_SKIP_EXTRACTION, skipExtraction);
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
