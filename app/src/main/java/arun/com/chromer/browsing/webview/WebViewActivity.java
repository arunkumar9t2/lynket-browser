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

package arun.com.chromer.browsing.webview;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.jetbrains.annotations.NotNull;

import arun.com.chromer.R;
import arun.com.chromer.browsing.BrowsingActivity;
import arun.com.chromer.browsing.customtabs.callbacks.ClipboardService;
import arun.com.chromer.browsing.customtabs.callbacks.FavShareBroadcastReceiver;
import arun.com.chromer.browsing.customtabs.callbacks.SecondaryBrowserReceiver;
import arun.com.chromer.browsing.openwith.OpenIntentWithActivity;
import arun.com.chromer.browsing.optionspopup.ChromerOptionsActivity;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import timber.log.Timber;

import static android.graphics.Color.WHITE;
import static arun.com.chromer.settings.Preferences.PREFERRED_ACTION_BROWSER;
import static arun.com.chromer.settings.Preferences.PREFERRED_ACTION_FAV_SHARE;
import static arun.com.chromer.settings.Preferences.PREFERRED_ACTION_GEN_SHARE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_ORIGINAL_URL;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;

public class WebViewActivity extends BrowsingActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.web_view)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setSupportActionBar(toolbar);

            final Website website = getIntent().getParcelableExtra(EXTRA_KEY_WEBSITE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(xyz.klinker.android.article.R.drawable.article_ic_close);
                getSupportActionBar().setTitle(website != null ? website.safeLabel() : getIntent().getDataString());
            }

            webView.setWebViewClient(new WebViewClient() {

            });
            final WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webView.loadUrl(getIntent().getDataString());

            if (Preferences.get(this).aggressiveLoading()) {
                delayedGoToBack();
            }
        } catch (InflateException e) {
            Timber.e(e);
            Toast.makeText(this, R.string.web_view_not_found, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_web_view;
    }

    private void delayedGoToBack() {
        new Handler().postDelayed(() -> moveTaskToBack(true), 650);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article_view_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem actionButton = menu.findItem(R.id.menu_action_button);
        switch (Preferences.get(this).preferredAction()) {
            case PREFERRED_ACTION_BROWSER:
                final String browser = Preferences.get(this).secondaryBrowserPackage();
                if (Utils.isPackageInstalled(this, browser)) {
                    actionButton.setTitle(R.string.choose_secondary_browser);
                    final ComponentName componentName = ComponentName.unflattenFromString(Preferences.get(this).secondaryBrowserComponent());
                    try {
                        actionButton.setIcon(getPackageManager().getActivityIcon(componentName));
                    } catch (PackageManager.NameNotFoundException e) {
                        actionButton.setVisible(false);
                    }
                } else {
                    actionButton.setVisible(false);
                }
                break;
            case PREFERRED_ACTION_FAV_SHARE:
                final String favShare = Preferences.get(this).favSharePackage();
                if (Utils.isPackageInstalled(this, favShare)) {
                    actionButton.setTitle(R.string.fav_share_app);
                    final ComponentName componentName = ComponentName.unflattenFromString(Preferences.get(this).favShareComponent());
                    try {
                        actionButton.setIcon(getPackageManager().getActivityIcon(componentName));
                    } catch (PackageManager.NameNotFoundException e) {
                        actionButton.setVisible(false);
                    }
                } else {
                    actionButton.setVisible(false);
                }
                break;
            case PREFERRED_ACTION_GEN_SHARE:
                actionButton.setIcon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_share_variant)
                        .color(WHITE)
                        .sizeDp(24));
                actionButton.setTitle(R.string.share);
                break;
        }
        final MenuItem fullPage = menu.findItem(R.id.menu_open_full_page);
        fullPage.setVisible(false);
        final MenuItem favoriteShare = menu.findItem(R.id.menu_share_with);
        final String pkg = Preferences.get(this).favSharePackage();
        if (pkg != null) {
            final String app = Utils.getAppNameWithPackage(this, pkg);
            final String label = String.format(getString(R.string.share_with), app);
            favoriteShare.setTitle(label);
        } else {
            favoriteShare.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_action_button:
                switch (Preferences.get(this).preferredAction()) {
                    case PREFERRED_ACTION_BROWSER:
                        sendBroadcast(new Intent(this, SecondaryBrowserReceiver.class)
                                .setData(getIntent().getData()));
                        break;
                    case PREFERRED_ACTION_FAV_SHARE:
                        sendBroadcast(new Intent(this, FavShareBroadcastReceiver.class)
                                .setData(getIntent().getData()));
                        break;
                    case PREFERRED_ACTION_GEN_SHARE:
                        shareUrl();
                        break;
                }
                break;
            case R.id.menu_copy_link:
                startService(new Intent(this, ClipboardService.class)
                        .setData(getIntent().getData()));
                break;
            case R.id.menu_open_with:
                startActivity(new Intent(this, OpenIntentWithActivity.class)
                        .setData(getIntent().getData()));
                break;
            case R.id.menu_share:
                shareUrl();
                break;
            case R.id.menu_more:
                final Intent moreMenuActivity = new Intent(this, ChromerOptionsActivity.class);
                moreMenuActivity.putExtra(EXTRA_KEY_ORIGINAL_URL, getIntent().getDataString());
                moreMenuActivity.setData(getIntent().getData());
                startActivity(moreMenuActivity);
                break;
            case R.id.menu_share_with:
                sendBroadcast(new Intent(this, FavShareBroadcastReceiver.class).setData(getIntent().getData()));
                break;
        }
        return true;
    }

    private void shareUrl() {
        Utils.shareText(this, getIntent().getDataString());
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onWebsiteLoaded(@NotNull Website website) {

    }

    @Override
    public void inject(@NonNull ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }
}
