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

package arun.com.chromer.activities.browsing.article;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.activities.MoreMenuActivity;
import arun.com.chromer.activities.OpenIntentWithActivity;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.customtabs.callbacks.ClipboardService;
import arun.com.chromer.customtabs.callbacks.FavShareBroadcastReceiver;
import arun.com.chromer.customtabs.callbacks.SecondaryBrowserReceiver;
import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.RxUtils;
import arun.com.chromer.util.Utils;
import timber.log.Timber;
import xyz.klinker.android.article.ArticleActivity;
import xyz.klinker.android.article.data.webarticle.model.WebArticle;

import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
import static android.graphics.Color.WHITE;
import static arun.com.chromer.activities.settings.Preferences.PREFERRED_ACTION_BROWSER;
import static arun.com.chromer.activities.settings.Preferences.PREFERRED_ACTION_FAV_SHARE;
import static arun.com.chromer.activities.settings.Preferences.PREFERRED_ACTION_GEN_SHARE;
import static arun.com.chromer.shared.Constants.ACTION_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_ARTICLE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_ORIGINAL_URL;

public class ChromerArticleActivity extends ArticleActivity {
    private String baseUrl = "";
    private BroadcastReceiver minimizeReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseUrl = getIntent().getDataString();
        registerMinimizeReceiver();
    }

    @Override
    protected void onArticleLoadingFailed(Throwable throwable) {
        super.onArticleLoadingFailed(throwable);
        // Start custom tab activity on the same task, silently killing this activity.
        final Intent customTabActivity = new Intent(this, CustomTabActivity.class);
        customTabActivity.setData(getIntent().getData());
        startActivity(customTabActivity);
        finish();
    }

    @Override
    protected void onArticleLoaded(@NonNull WebArticle webArticle) {
        super.onArticleLoaded(webArticle);
        HistoryRepository.getInstance(this)
                .insert(WebSite.fromArticle(webArticle))
                .compose(RxUtils.applySchedulers())
                .subscribe();
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
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_action_button:
                switch (Preferences.get(this).preferredAction()) {
                    case PREFERRED_ACTION_BROWSER:
                        sendBroadcast(new Intent(this, SecondaryBrowserReceiver.class).setData(Uri.parse(baseUrl)));
                        break;
                    case PREFERRED_ACTION_FAV_SHARE:
                        sendBroadcast(new Intent(this, FavShareBroadcastReceiver.class).setData(Uri.parse(baseUrl)));
                        break;
                    case PREFERRED_ACTION_GEN_SHARE:
                        shareUrl();
                        break;
                }
                break;
            case R.id.menu_copy_link:
                startService(new Intent(this, ClipboardService.class).setData(Uri.parse(baseUrl)));
                break;
            case R.id.menu_open_with:
                startActivity(new Intent(this, OpenIntentWithActivity.class).setData(Uri.parse(baseUrl)));
                break;
            case R.id.menu_share:
                shareUrl();
                break;
            case R.id.menu_open_full_page:
                final Intent customTabActivity = new Intent(this, CustomTabActivity.class);
                customTabActivity.setData(Uri.parse(baseUrl));
                if (Preferences.get(this).mergeTabs()) {
                    customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    customTabActivity.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK);
                }
                startActivity(customTabActivity);
                finish();
                break;
            case R.id.menu_more:
                final Intent moreMenuActivity = new Intent(this, MoreMenuActivity.class);
                moreMenuActivity.setData(getIntent().getData());
                moreMenuActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                moreMenuActivity.putExtra(EXTRA_KEY_ORIGINAL_URL, baseUrl);
                moreMenuActivity.putExtra(EXTRA_KEY_FROM_ARTICLE, true);
                startActivity(moreMenuActivity);
                break;
            case R.id.menu_share_with:
                sendBroadcast(new Intent(this, FavShareBroadcastReceiver.class).setData(Uri.parse(baseUrl)));
                break;
        }
        return true;
    }

    private void shareUrl() {
        Utils.shareText(this, baseUrl);
    }

    private void registerMinimizeReceiver() {
        minimizeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(ACTION_MINIMIZE) && intent.hasExtra(EXTRA_KEY_ORIGINAL_URL)) {
                    final String url = intent.getStringExtra(EXTRA_KEY_ORIGINAL_URL);
                    if (baseUrl.equalsIgnoreCase(url)) {
                        try {
                            Timber.d("Minimized %s", url);
                            moveTaskToBack(true);
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(minimizeReceiver, new IntentFilter(ACTION_MINIMIZE));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(minimizeReceiver);
        super.onDestroy();
    }
}
