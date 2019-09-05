/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.bubbles.webheads;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.browsing.article.ArticlePreloader;
import arun.com.chromer.browsing.customtabs.CustomTabManager;
import arun.com.chromer.browsing.newtab.NewTabDialogActivity;
import arun.com.chromer.bubbles.webheads.physics.SpringChain2D;
import arun.com.chromer.bubbles.webheads.ui.WebHeadContract;
import arun.com.chromer.bubbles.webheads.ui.context.WebHeadContextActivity;
import arun.com.chromer.bubbles.webheads.ui.views.Trashy;
import arun.com.chromer.bubbles.webheads.ui.views.WebHead;
import arun.com.chromer.data.website.WebsiteRepository;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.di.service.ServiceComponent;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.tabs.TabsManager;
import arun.com.chromer.util.SchedulerProvider;
import arun.com.chromer.util.Utils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.widget.Toast.LENGTH_SHORT;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static arun.com.chromer.shared.Constants.ACTION_CLOSE_WEBHEAD_BY_URL;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBHEAD_DELETED;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBSITE_UPDATED;
import static arun.com.chromer.shared.Constants.ACTION_OPEN_CONTEXT_ACTIVITY;
import static arun.com.chromer.shared.Constants.ACTION_OPEN_NEW_TAB;
import static arun.com.chromer.shared.Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION;
import static arun.com.chromer.shared.Constants.ACTION_STOP_WEBHEAD_SERVICE;
import static arun.com.chromer.shared.Constants.ACTION_WEBHEAD_COLOR_SET;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_AMP;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_INCOGNITO;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_REBIND_WEBHEAD_CXN;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBHEAD_COLOR;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;
import static arun.com.chromer.shared.Constants.NO_COLOR;

public class WebHeadService extends OverlayService implements WebHeadContract,
        CustomTabManager.ConnectionCallback {
    // Max visible web heads is set 6 for performance reasons.
    public static final int MAX_VISIBLE_WEB_HEADS = 5;
    // Connection manager instance to connect and warm up custom tab providers
    private static CustomTabManager customTabManager;
    /**
     * Reference to all the web heads created on screen. Ordered in the order of creation by using
     * {@link LinkedHashMap}. The key must be unique and is usually the url the web head represents.
     */
    private final Map<String, WebHead> webHeads = new LinkedHashMap<>();
    // The base spring system to create our springs.
    private final SpringSystem springSystem = SpringSystem.create();
    private final CompositeSubscription subs = new CompositeSubscription();
    private final BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_REBIND_WEBHEAD_TAB_CONNECTION:
                    final boolean shouldRebind = intent.getBooleanExtra(EXTRA_KEY_REBIND_WEBHEAD_CXN, false);
                    if (shouldRebind) {
                        bindToCustomTabSession();
                    }
                    break;
                case ACTION_WEBHEAD_COLOR_SET:
                    final int webHeadColor = intent.getIntExtra(EXTRA_KEY_WEBHEAD_COLOR, NO_COLOR);
                    if (webHeadColor != NO_COLOR) {
                        updateWebHeadColors(webHeadColor);
                    }
                    break;
                case ACTION_CLOSE_WEBHEAD_BY_URL:
                    final Website website = intent.getParcelableExtra(EXTRA_KEY_WEBSITE);
                    if (website != null) {
                        closeWebHeadByUrl(website.url);
                    }
                    break;
            }
        }
    };
    private final BroadcastReceiver notificationActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_STOP_WEBHEAD_SERVICE:
                    stopService();
                    break;
                case ACTION_OPEN_CONTEXT_ACTIVITY:
                    openContextActivity();
                    break;
                case ACTION_OPEN_NEW_TAB:
                    final Intent newTabIntent = new Intent(context, NewTabDialogActivity.class);
                    newTabIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newTabIntent);
                    break;
            }
        }
    };
    @Inject
    WebsiteRepository websiteRepository;

    @Inject
    TabsManager tabsManager;

    @Inject
    ArticlePreloader articlePreloader;
    // Clubbed movement manager
    private SpringChain2D springChain2D;
    // State variable to know if we connected successfully to CT provider.
    private boolean customTabConnected;

    public static CustomTabsSession getTabSession() {
        if (customTabManager != null) {
            return customTabManager.getSession();
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int getNotificationId() {
        // Constant
        return 1;
    }

    @NonNull
    @Override
    public Notification getNotification() {
        if (Utils.ANDROID_OREO) {
            final NotificationChannel channel = new NotificationChannel(WebHeadService.class.getName(), getString(R.string.web_heads_service), NotificationManager.IMPORTANCE_MIN);
            channel.setDescription(getString(R.string.app_detection_notification_channel_description));
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        final PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_STOP_WEBHEAD_SERVICE), FLAG_UPDATE_CURRENT);
        final PendingIntent contextActivity = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPEN_CONTEXT_ACTIVITY), FLAG_UPDATE_CURRENT);
        final PendingIntent newTab = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPEN_NEW_TAB), FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, WebHeadService.class.getName())
                .setSmallIcon(R.drawable.ic_chromer_notification)
                .setPriority(PRIORITY_MIN)
                .setContentText(getString(R.string.tap_close_all))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .addAction(R.drawable.ic_add, getText(R.string.open_new_tab), newTab)
                .addAction(R.drawable.ic_list, getText(R.string.manage), contextActivity)
                .setContentTitle(getString(R.string.web_heads_service))
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        return notification;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                stopService();
                return;
            }
        }
        springChain2D = SpringChain2D.create(this);
        Trashy.init(this);
        bindToCustomTabSession();
        registerReceivers();
    }

    @Override
    protected void inject(ServiceComponent serviceComponent) {
        serviceComponent.inject(this);
    }

    @Override
    public void onDestroy() {
        Timber.d("Exiting webhead service");
        subs.clear();
        WebHead.clearMasterPosition();
        removeWebHeads();
        if (customTabManager != null) {
            customTabManager.unbindCustomTabsService(this);
        }
        Trashy.destroy();
        unregisterReceivers();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkForOverlayPermission();
        processIntent(intent);
        return START_STICKY;
    }

    private void processIntent(@Nullable Intent intent) {
        if (intent == null || intent.getDataString() == null) return; // don't do anything

        final boolean isForMinimized = intent.getBooleanExtra(EXTRA_KEY_MINIMIZE, false);
        final boolean isFromAmp = intent.getBooleanExtra(EXTRA_KEY_FROM_AMP, false);
        final boolean isIncognito = intent.getBooleanExtra(EXTRA_KEY_INCOGNITO, false);

        final String urlToLoad = intent.getDataString();
        if (TextUtils.isEmpty(urlToLoad)) {
            Toast.makeText(this, R.string.invalid_link, LENGTH_SHORT).show();
            return;
        }

        if (!isLinkAlreadyLoaded(urlToLoad)) {
            addWebHead(urlToLoad, isFromAmp, isIncognito);
        } else if (!isForMinimized) {
            Toast.makeText(this, R.string.already_loaded, LENGTH_SHORT).show();
        }
    }

    private boolean isLinkAlreadyLoaded(@Nullable String urlToLoad) {
        return urlToLoad == null || webHeads.containsKey(urlToLoad);
    }

    private void addWebHead(final String webHeadUrl, boolean isFromAmp, boolean isIncognito) {
        if (springChain2D == null) {
            springChain2D = SpringChain2D.create(this);
        }
        springChain2D.clear();

        final WebHead newWebHead = new WebHead(/*Service*/ this, webHeadUrl, /*listener*/ this);
        for (WebHead oldWebHead : webHeads.values()) {
            // Set all old web heads to slave
            oldWebHead.setMaster(false);
        }
        newWebHead.setMaster(true);
        newWebHead.setFromAmp(isFromAmp);
        newWebHead.setIncognito(isIncognito);

        // Add to our map
        webHeads.put(webHeadUrl, newWebHead);

        reveal(newWebHead);

        preLoadForArticle(webHeadUrl);

        doExtraction(webHeadUrl, isIncognito);
    }

    private boolean reveal(WebHead newWebHead) {
        return newWebHead.post(() -> newWebHead.reveal(() -> {
            // Update the spring chain
            updateSpringChain();
            // Trigger an update
            onMasterWebHeadMoved(newWebHead.getWindowParams().x, newWebHead.getWindowParams().y);
        }));
    }

    private void doExtraction(final String webHeadUrl, boolean isIncognito) {
        final Observable<Website> websiteObservable;
        if (!isIncognito) {
            websiteObservable = websiteRepository.getWebsite(webHeadUrl);
        } else {
            websiteObservable = websiteRepository.getWebsiteReadOnly(webHeadUrl);
        }
        //noinspection Convert2MethodRef
        subs.add(websiteObservable
                .filter(website -> website != null)
                .compose(SchedulerProvider.applyIoSchedulers())
                .doOnNext(website -> {
                    final WebHead webHead = webHeads.get(webHeadUrl);
                    if (webHead != null) {
                        warmUp(webHead);
                        webHead.setWebsite(website);
                        ContextActivityHelper.signalUpdated(getApplication(), webHead.getWebsite());
                    }
                })
                .observeOn(Schedulers.io())
                .map(website -> websiteRepository.getWebsiteRoundIconAndColor(website))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(faviconColor -> {
                    final WebHead webHead = webHeads.get(webHeadUrl);
                    if (webHead != null) {
                        if (faviconColor.first != null) {
                            webHead.setFaviconDrawable(faviconColor.first);
                        }
                        if (faviconColor.second != Constants.NO_COLOR) {
                            webHead.setWebHeadColor(faviconColor.second);
                        }
                    }
                }, Timber::e));
    }

    private void bindToCustomTabSession() {
        if (customTabManager != null) {
            // Already an instance exists, so we will un bind the current connection and then
            // bind again.
            Timber.d("Severing existing connection");
            customTabManager.unbindCustomTabsService(this);
        }

        customTabManager = new CustomTabManager();
        customTabManager.setConnectionCallback(this);
        customTabManager.setNavigationCallback(new WebHeadNavigationCallback());

        if (customTabManager.bindCustomTabsService(this)) Timber.d("Binding successful");
    }

    private void warmUp(WebHead webHead) {
        if (!Preferences.get(this).aggressiveLoading()) {
            if (customTabConnected) {
                preLoadUrl(webHead.getUnShortenedUrl());
            } else {
                deferPreload(webHead.getUnShortenedUrl());
            }
        }
    }

    /**
     * Based on the current active browsing mode, will perform correct preload strategy.
     * If its normal, then do custom tab may launch url else if its article mode, then call
     * article mode's prefetch.
     */
    private void preLoadUrl(final String url) {
        if (!Preferences.get(this).articleMode()) {
            customTabManager.mayLaunchUrl(Uri.parse(url));
        }
    }

    private void preLoadForArticle(String url) {
        if (Preferences.get(this).articleMode()) {
            articlePreloader.preloadArticle(Uri.parse(url), success -> Timber.d("Url %s preloaded, result: %b", url, success));
        }
    }

    private void deferPreload(@NonNull final String urlToLoad) {
        new Handler().postDelayed(() -> preLoadUrl(urlToLoad), 300);
    }

    private void removeWebHeads() {
        for (WebHead webhead : webHeads.values()) {
            if (webhead != null) webhead.destroySelf(false);
        }
        // Since no callback is received clear the map manually.
        webHeads.clear();
        springChain2D.clear();
        Timber.d("WebHeads: %d", webHeads.size());
    }

    private boolean shouldQueue(final int index) {
        return index > MAX_VISIBLE_WEB_HEADS;
    }

    private void updateWebHeadColors(@ColorInt int webHeadColor) {
        final AnimatorSet animatorSet = new AnimatorSet();
        final List<Animator> animators = new LinkedList<>();
        for (WebHead webhead : webHeads.values()) {
            animators.add(webhead.getRevealAnimator(webHeadColor));
        }
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    private void selectNextMaster() {
        final ListIterator<String> it = new ArrayList<>(webHeads.keySet()).listIterator(webHeads.size());
        //noinspection LoopStatementThatDoesntLoop
        while (it.hasPrevious()) {
            final String key = it.previous();
            final WebHead toBeMaster = webHeads.get(key);
            if (toBeMaster != null) {
                toBeMaster.setMaster(true);
                updateSpringChain();
                toBeMaster.goToMasterTouchDownPoint();
            }
            break;
        }
    }

    private void updateSpringChain() {
        springChain2D.rest();
        springChain2D.clear();
        springChain2D.disableDisplacement();
        // Index that is used to differentiate spring config
        int springChainIndex = webHeads.values().size();
        // Index that is used to determine if the web hed should be in queue.
        int index = webHeads.values().size();
        for (final WebHead webHead : webHeads.values()) {
            if (webHead != null) {
                if (webHead.isMaster()) {
                    // Master will never be in queue, so no check is made.
                    springChain2D.setMasterSprings(webHead.getXSpring(), webHead.getYSpring());
                } else {
                    if (shouldQueue(index)) {
                        webHead.setInQueue(true);
                    } else {
                        webHead.setInQueue(false);
                        // We should add the springs to our chain only if the web head is active
                        webHead.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(90, 9 + (springChainIndex * 5)));
                        springChain2D.addSlaveSprings(webHead.getXSpring(), webHead.getYSpring());
                    }
                    springChainIndex--;
                }
                index--;
            }
        }
        springChain2D.enableDisplacement();
    }

    @Override
    public void onWebHeadClick(@NonNull WebHead webHead) {
        tabsManager.openUrl(this, webHead.getWebsite(), true, true, false, webHead.isFromAmp(), webHead.isIncognito());

        // If user prefers to the close the head on opening the link, then call destroySelf()
        // which will take care of closing and detaching the web head
        if (Preferences.get(this).webHeadsCloseOnOpen()) {
            webHead.destroySelf(true);
        }
        hideTrashy();
    }

    @Override
    public void onWebHeadDestroyed(@NonNull WebHead webHead, boolean isLastWebHead) {
        webHead.setMaster(false);
        webHeads.remove(webHead.getUrl());
        if (isLastWebHead) {
            Trashy.get(this).destroyAnimator(this::stopService);
        } else {
            selectNextMaster();
            if (!Preferences.get(this).articleMode()) {
                preLoadUrl("");
            }
        }
        ContextActivityHelper.signalDeleted(this, webHead.getWebsite());
    }

    @Override
    public void onMasterWebHeadMoved(int x, int y) {
        springChain2D.performGroupMove(x, y);
    }

    @NonNull
    @Override
    public Spring newSpring() {
        return springSystem.createSpring();
    }

    @Override
    public void onMasterLockedToTrashy() {
        springChain2D.disableDisplacement();
    }

    @Override
    public void onMasterReleasedFromTrashy() {
        springChain2D.enableDisplacement();
    }

    @Override
    public void closeAll() {
        stopService();
    }

    @Override
    public void onMasterLongClick() {
        openContextActivity();
    }

    private void openContextActivity() {
        final ListIterator<String> it = new ArrayList<>(webHeads.keySet()).listIterator(webHeads.size());
        final ArrayList<Website> websites = new ArrayList<>();
        while (it.hasPrevious()) {
            final String key = it.previous();
            final WebHead webHead = webHeads.get(key);
            if (webHead != null) {
                websites.add(webHead.getWebsite());
            }
        }
        ContextActivityHelper.open(this, websites);
    }

    @Override
    public void onCustomTabsConnected() {
        customTabConnected = true;
        Timber.d("Connected to custom tabs successfully");
    }

    @Override
    public void onCustomTabsDisconnected() {
        customTabConnected = false;
    }

    private void closeWebHeadByUrl(@NonNull String url) {
        final WebHead webHead = webHeads.get(url);
        if (webHead != null) {
            webHead.destroySelf(true);
        }
    }

    private void hideTrashy() {
        Trashy.disappear();
    }

    private void registerReceivers() {
        final IntentFilter localEvents = new IntentFilter();
        localEvents.addAction(ACTION_WEBHEAD_COLOR_SET);
        localEvents.addAction(ACTION_REBIND_WEBHEAD_TAB_CONNECTION);
        localEvents.addAction(ACTION_CLOSE_WEBHEAD_BY_URL);
        localEvents.addAction(ACTION_OPEN_CONTEXT_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, localEvents);

        final IntentFilter notificationFilter = new IntentFilter();
        notificationFilter.addAction(ACTION_STOP_WEBHEAD_SERVICE);
        notificationFilter.addAction(ACTION_OPEN_CONTEXT_ACTIVITY);
        notificationFilter.addAction(ACTION_OPEN_NEW_TAB);
        registerReceiver(notificationActionReceiver, notificationFilter);
    }

    private void unregisterReceivers() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
            unregisterReceiver(notificationActionReceiver);
        } catch (IllegalArgumentException ignored) {
            Timber.e(ignored);
        }
    }

    private static class ContextActivityHelper {
        static void signalUpdated(Context context, Website website) {
            final Intent intent = new Intent(ACTION_EVENT_WEBSITE_UPDATED);
            intent.putExtra(EXTRA_KEY_WEBSITE, website);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        static void signalDeleted(Context context, Website website) {
            final Intent intent = new Intent(ACTION_EVENT_WEBHEAD_DELETED);
            intent.putExtra(EXTRA_KEY_WEBSITE, website);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        static void open(Context context, ArrayList<Website> websites) {
            final Intent intent = new Intent(context, WebHeadContextActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
            intent.putParcelableArrayListExtra(EXTRA_KEY_WEBSITE, websites);
            context.startActivity(intent);
        }
    }

    private class WebHeadNavigationCallback extends CustomTabManager.NavigationCallback {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            switch (navigationEvent) {
                case TAB_SHOWN:
                    break;
                case TAB_HIDDEN:
                    break;
            }
        }
    }
}