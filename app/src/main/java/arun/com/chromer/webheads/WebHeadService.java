package arun.com.chromer.webheads;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import arun.com.chromer.R;
import arun.com.chromer.activities.NewTabDialogActivity;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.data.website.WebsiteRepository;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.webheads.helper.ColorExtractionTask;
import arun.com.chromer.webheads.physics.SpringChain2D;
import arun.com.chromer.webheads.ui.WebHeadContract;
import arun.com.chromer.webheads.ui.context.WebHeadContextActivity;
import arun.com.chromer.webheads.ui.views.Trashy;
import arun.com.chromer.webheads.ui.views.WebHead;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import xyz.klinker.android.article.ArticleUtils;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;
import static android.widget.Toast.LENGTH_SHORT;
import static arun.com.chromer.shared.Constants.ACTION_CLOSE_WEBHEAD_BY_URL;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBHEAD_DELETED;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBSITE_UPDATED;
import static arun.com.chromer.shared.Constants.ACTION_OPEN_CONTEXT_ACTIVITY;
import static arun.com.chromer.shared.Constants.ACTION_OPEN_NEW_TAB;
import static arun.com.chromer.shared.Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION;
import static arun.com.chromer.shared.Constants.ACTION_STOP_WEBHEAD_SERVICE;
import static arun.com.chromer.shared.Constants.ACTION_WEBHEAD_COLOR_SET;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_NEW_TAB;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_REBIND_WEBHEAD_CXN;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBHEAD_COLOR;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;
import static arun.com.chromer.shared.Constants.NO_COLOR;

public class WebHeadService extends OverlayService implements WebHeadContract,
        CustomTabManager.ConnectionCallback {
    /**
     * Reference to all the web heads created on screen. Ordered in the order of creation by using
     * {@link LinkedHashMap}. The key must be unique and is usually the url the web head represents.
     */
    private final Map<String, WebHead> webHeads = new LinkedHashMap<>();
    // Connection manager instance to connect and warm up custom tab providers
    private static CustomTabManager customTabManager;
    // The base spring system to create our springs.
    private final SpringSystem springSystem = SpringSystem.create();
    // Clubbed movement manager
    private SpringChain2D springChain2D;
    // State variable to know if we connected successfully to CT provider.
    private boolean customTabConnected;
    // Max visible web heads is set 6 for performance reasons.
    public static final int MAX_VISIBLE_WEB_HEADS = 5;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    int getNotificationId() {
        // Constant
        return 1;
    }

    @Override
    Notification getNotification() {
        final PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_STOP_WEBHEAD_SERVICE), FLAG_UPDATE_CURRENT);
        final PendingIntent contextActivity = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPEN_CONTEXT_ACTIVITY), FLAG_UPDATE_CURRENT);
        final PendingIntent newTab = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPEN_NEW_TAB), FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chromer_notification)
                .setPriority(PRIORITY_MIN)
                .setContentText(getString(R.string.tap_close_all))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .addAction(R.drawable.ic_add, getText(R.string.new_tab), newTab)
                .addAction(R.drawable.ic_list, getText(R.string.manage), contextActivity)
                .setContentTitle(getString(R.string.web_heads_service))
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                stopSelf();
                return;
            }
        }
        springChain2D = SpringChain2D.create(this);
        Trashy.init(this);
        bindToCustomTabSession();
        registerReceivers();
    }

    @Override
    public void onDestroy() {
        Timber.d("Exiting webhead service");
        WebHead.clearMasterPosition();
        removeWebHeads();
        compositeSubscription.clear();
        if (customTabManager != null) {
            customTabManager.unbindCustomTabsService(this);
        }
        Trashy.destroy();
        unregisterReceivers();
        super.onDestroy();
    }

    public static CustomTabsSession getTabSession() {
        if (customTabManager != null) {
            return customTabManager.getSession();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkForOverlayPermission();
        processIntent(intent);
        return START_STICKY;
    }

    private void processIntent(@Nullable Intent intent) {
        if (intent == null || intent.getDataString() == null) return; // don't do anything
        final boolean isFromNewTab = intent.getBooleanExtra(EXTRA_KEY_FROM_NEW_TAB, false);
        final boolean isForMinimized = intent.getBooleanExtra(EXTRA_KEY_MINIMIZE, false);

        final String urlToLoad = intent.getDataString();
        if (TextUtils.isEmpty(urlToLoad)) {
            Toast.makeText(this, R.string.invalid_link, LENGTH_SHORT).show();
            return;
        }

        if (!isLinkAlreadyLoaded(urlToLoad)) {
            addWebHead(urlToLoad, isFromNewTab, isForMinimized);
        } else if (!isForMinimized) {
            Toast.makeText(this, R.string.already_loaded, LENGTH_SHORT).show();
        }
    }

    private boolean isLinkAlreadyLoaded(@Nullable String urlToLoad) {
        return urlToLoad == null || webHeads.containsKey(urlToLoad);
    }

    private void addWebHead(final String webHeadUrl, final boolean isNewTab, final boolean isMinimized) {
        if (springChain2D == null) {
            springChain2D = SpringChain2D.create(this);
        }
        springChain2D.clear();

        final WebHead newWebHead = new WebHead(/*Service*/ this, webHeadUrl, /*listener*/ this);
        newWebHead.setFromNewTab(isNewTab);
        for (WebHead oldWebHead : webHeads.values()) {
            // Set all old web heads to slave
            oldWebHead.setMaster(false);
        }
        newWebHead.setMaster(true);
        // Add to our map
        webHeads.put(webHeadUrl, newWebHead);

        if (Preferences.get(getApplication()).aggressiveLoading() && !isMinimized && !Preferences.get(this).articleMode()) {
            DocumentUtils.openNewCustomTab(getApplication(), newWebHead.getWebsite(), isNewTab);
            new Handler().postDelayed(() -> reveal(newWebHead), 650);
        } else {
            reveal(newWebHead);
        }

        preLoadForArticle(webHeadUrl);

        // Begin metadata extractions
        doExtraction(webHeadUrl);
    }

    private boolean reveal(WebHead newWebHead) {
        return newWebHead.post(() -> newWebHead.reveal(() -> {
            // Update the spring chain
            updateSpringChain();
            // Trigger an update
            onMasterWebHeadMoved(newWebHead.getWindowParams().x, newWebHead.getWindowParams().y);
        }));
    }

    private void doExtraction(final String webHeadUrl) {
        final Subscription s = WebsiteRepository.getInstance(this)
                .getWebsite(webHeadUrl)
                .doOnError(Timber::e)
                .doOnNext(webSite -> {
                    final WebHead webHead = webHeads.get(webHeadUrl);
                    if (webHead != null && webSite != null) {
                        warmUp(webHead);
                        webHead.setWebSite(webSite);
                        ContextActivityHelper.signalUpdated(getApplication(), webHead.getWebsite());
                        final String faviconUrl = webSite.faviconUrl;
                        Glide.with(getApplication())
                                .load(faviconUrl)
                                .asBitmap()
                                .into(new BitmapImageViewTarget(webHead.getFaviconView()) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        if (resource != null) {
                                            // dispatch themeColor extraction task
                                            new ColorExtractionTask(webHead, resource).executeOnExecutor(THREAD_POOL_EXECUTOR);
                                            webHead.setFaviconDrawable(getRoundedBitmapDrawable(resource));
                                        }
                                    }
                                });
                    } else if (webHead != null) {
                        warmUp(webHead);
                    }
                }).subscribe();
        compositeSubscription.add(s);
    }

    @NonNull
    private RoundedBitmapDrawable getRoundedBitmapDrawable(Bitmap resource) {
        final RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
        roundedBitmapDrawable.setAntiAlias(true);
        roundedBitmapDrawable.setCircular(true);
        return roundedBitmapDrawable;
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
            ArticleUtils.preloadArticle(this, Uri.parse(url),
                    success -> Timber.d("Url %s preloaded, result: %b", url, success));
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onWebHeadClick(@NonNull WebHead webHead) {
        DocumentUtils.smartOpenNewTab(this, webHead.getWebsite());

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
            Trashy.get(this).destroyAnimator(this::stopSelf);
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
        stopSelf();
    }

    @Override
    public void onMasterLongClick() {
        openContextActivity();
    }

    private void openContextActivity() {
        final ListIterator<String> it = new ArrayList<>(webHeads.keySet()).listIterator(webHeads.size());
        final ArrayList<WebSite> webSites = new ArrayList<>();
        while (it.hasPrevious()) {
            final String key = it.previous();
            final WebHead webHead = webHeads.get(key);
            if (webHead != null) {
                webSites.add(webHead.getWebsite());
            }
        }
        ContextActivityHelper.open(this, webSites);
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        unregisterReceiver(notificationActionReceiver);
    }

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
                    final WebSite webSite = intent.getParcelableExtra(EXTRA_KEY_WEBSITE);
                    if (webSite != null) {
                        closeWebHeadByUrl(webSite.url);
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
                    stopSelf();
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


    private static class ContextActivityHelper {
        static void signalUpdated(Context context, WebSite webSite) {
            final Intent intent = new Intent(ACTION_EVENT_WEBSITE_UPDATED);
            intent.putExtra(EXTRA_KEY_WEBSITE, webSite);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        static void signalDeleted(Context context, WebSite webSite) {
            final Intent intent = new Intent(ACTION_EVENT_WEBHEAD_DELETED);
            intent.putExtra(EXTRA_KEY_WEBSITE, webSite);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        static void open(Context context, ArrayList<WebSite> webSites) {
            final Intent intent = new Intent(context, WebHeadContextActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
            intent.putParcelableArrayListExtra(EXTRA_KEY_WEBSITE, webSites);
            context.startActivity(intent);
        }
    }
}