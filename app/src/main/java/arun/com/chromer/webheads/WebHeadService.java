package arun.com.chromer.webheads;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chimbori.crux.articles.Article;
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
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.webheads.helper.ColorExtractionTask;
import arun.com.chromer.webheads.helper.RxParser;
import arun.com.chromer.webheads.helper.UrlOrganizer;
import arun.com.chromer.webheads.helper.WebSite;
import arun.com.chromer.webheads.physics.SpringChain2D;
import arun.com.chromer.webheads.ui.WebHeadContract;
import arun.com.chromer.webheads.ui.context.WebHeadContextActivity;
import arun.com.chromer.webheads.ui.views.RemoveWebHead;
import arun.com.chromer.webheads.ui.views.WebHead;
import timber.log.Timber;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;
import static arun.com.chromer.shared.Constants.ACTION_CLOSE_WEBHEAD_BY_URL;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBHEAD_DELETED;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBSITE_UPDATED;
import static arun.com.chromer.shared.Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION;
import static arun.com.chromer.shared.Constants.ACTION_STOP_WEBHEAD_SERVICE;
import static arun.com.chromer.shared.Constants.ACTION_WEBHEAD_COLOR_SET;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_NEW_TAB;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_REBIND_WEBHEAD_CXN;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBHEAD_COLOR;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;
import static arun.com.chromer.shared.Constants.NO_COLOR;

public class WebHeadService extends Service implements WebHeadContract,
        CustomTabManager.ConnectionCallback, RxParser.OnParseListener {
    /**
     * Reference to all the web heads created on screen. Ordered in the order of creation by using
     * {@link LinkedHashMap}. The key must be unique and is usually the url the web head represents.
     */
    private final Map<String, WebHead> webHeads = new LinkedHashMap<>();
    private static String lastOpenedUrl = "";
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
    // Url organizer to take care of pre-fetching.
    private UrlOrganizer urlOrganizer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Timber.d("Exited web head service since overlay permission was revoked");
                stopSelf();
                return;
            }
        }
        springChain2D = SpringChain2D.create(this);
        urlOrganizer = new UrlOrganizer(this);
        RemoveWebHead.init(this);

        bindToCustomTabSession();
        registerReceivers();
        showNotification();
        RxParser.getInstance().setOnParseListener(this);
    }

    @Override
    public void onDestroy() {
        Timber.d("Exiting webhead service");
        stopForeground(true);
        WebHead.clearMasterPosition();
        removeWebHeads();
        if (customTabManager != null) {
            customTabManager.unbindCustomTabsService(this);
        }
        RemoveWebHead.destroy();
        unregisterReceivers();
        RxParser.getInstance().unsubscribe();
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
        processIntent(intent);
        return START_STICKY;
    }

    private void showNotification() {
        final PendingIntent contentIntent = PendingIntent.getBroadcast(this,
                0,
                new Intent(ACTION_STOP_WEBHEAD_SERVICE),
                FLAG_UPDATE_CURRENT);

        final Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chromer_notification)
                .setPriority(PRIORITY_MIN)
                .setContentText(getString(R.string.tap_close_all))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(getString(R.string.web_heads_service))
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .build();

        startForeground(1, notification);
    }

    private void processIntent(@Nullable Intent intent) {
        if (intent == null || intent.getDataString() == null) return; // don't do anything
        final boolean isFromNewTab = intent.getBooleanExtra(EXTRA_KEY_FROM_NEW_TAB, false);
        final boolean isMinimized = intent.getBooleanExtra(EXTRA_KEY_MINIMIZE, false);

        final String urlToLoad = intent.getDataString();
        if (!isLinkAlreadyLoaded(urlToLoad)) {
            addWebHead(urlToLoad, isFromNewTab, isMinimized);
        } else if (!isMinimized) {
            Toast.makeText(this, R.string.already_loaded, Toast.LENGTH_SHORT).show();
        }
    }

    private void addWebHead(final String webHeadUrl, final boolean isNewTab, final boolean isMinimized) {
        RxParser.getInstance().parse(webHeadUrl);
        springChain2D.clear();

        final WebHead newWebHead = new WebHead(/*Service*/ this, webHeadUrl, /*listener*/ this);
        newWebHead.setFromNewTab(isNewTab);

        springChain2D.setMasterSprings(newWebHead.getXSpring(), newWebHead.getYSpring());

        int index = webHeads.values().size();
        for (WebHead oldWebHead : webHeads.values()) {
            oldWebHead.setMaster(false);
            if (shouldQueue(index + 1)) {
                oldWebHead.setInQueue(true);
            } else {
                oldWebHead.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(90, 9 + (index * 5)));
                springChain2D.addSlaveSprings(oldWebHead.getXSpring(), oldWebHead.getYSpring());
            }
            index--;
        }
        springChain2D.rest();

        newWebHead.reveal();
        webHeads.put(webHeadUrl, newWebHead);

        if (Preferences.aggressiveLoading(this) && !isMinimized) {
            DocumentUtils.openNewCustomTab(this, newWebHead);
        }
    }

    private boolean shouldQueue(final int index) {
        return index > MAX_VISIBLE_WEB_HEADS;
    }

    @Override
    public void onUrlParsed(@NonNull final String url, final @Nullable Article article) {
        final WebHead webHead = webHeads.get(url);
        if (webHead != null && article != null) {
            warmUp(webHead);
            try {
                final String faviconUrl = article.faviconUrl;
                webHead.setWebSite(WebSite.fromArticle(article));
                Glide.with(this)
                        .load(faviconUrl)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(webHead.getFaviconView()) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (resource == null) {
                                    return;
                                }
                                // dispatch themeColor extraction task
                                new ColorExtractionTask(webHead, resource).executeOnExecutor(THREAD_POOL_EXECUTOR);

                                final RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                                roundedBitmapDrawable.setAntiAlias(true);
                                roundedBitmapDrawable.setCircular(true);
                                webHead.setFaviconDrawable(roundedBitmapDrawable);
                            }
                        });
                // Also signal the context activity so that it can update its data
                ContextActivityHelper.signalUpdated(this, webHead.getWebsite());
            } catch (Exception e) {
                Timber.e(e.getMessage());
            }
        } else if (webHead != null) {
            warmUp(webHead);
        }
    }

    private void warmUp(WebHead webHead) {
        if (!Preferences.aggressiveLoading(this)) {
            if (customTabConnected) {
                customTabManager.mayLaunchUrl(Uri.parse(webHead.getUnShortenedUrl()), null, urlOrganizer.getPossibleUrls(webHeads));
            } else deferMayLaunchUntilConnected(webHead.getUnShortenedUrl());
        }
    }

    private boolean isLinkAlreadyLoaded(@Nullable String urlToLoad) {
        return urlToLoad == null || webHeads.containsKey(urlToLoad);
    }

    // TODO Rework this crappy logic.
    private void deferMayLaunchUntilConnected(final String urlToLoad) {
        final Thread deferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 10) {
                    try {
                        if (customTabConnected) {
                            Thread.sleep(300);
                            boolean ok = customTabManager.mayLaunchUrl(Uri.parse(urlToLoad),
                                    null,
                                    urlOrganizer.getPossibleUrls(webHeads));
                            Timber.d("Deferred may launch was %b", ok);
                            if (ok) break;
                        }
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
            }
        });
        deferThread.start();
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

    private void removeWebHeads() {
        for (WebHead webhead : webHeads.values()) {
            if (webhead != null) webhead.destroySelf(false);
        }
        // Since no callback is received clear the map manually.
        webHeads.clear();
        springChain2D.clear();
        Timber.d("WebHeads: %d", webHeads.size());
    }

    private void updateWebHeadColors(@ColorInt int webHeadColor) {
        final AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animators = new LinkedList<>();
        for (WebHead webhead : webHeads.values()) {
            animators.add(webhead.getRevealAnimator(webHeadColor));
        }
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    private void updateSpringChain() {
        springChain2D.rest();
        springChain2D.clear();
        springChain2D.enableDisplacement();
        // Index that is used to differentiate spring config
        int springChainIndex = webHeads.values().size();
        // Index that is used to determine if the web hed should be in queue.
        int index = webHeads.values().size();
        for (WebHead webHead : webHeads.values()) {
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onWebHeadClick(@NonNull WebHead webHead) {
        if (!webHead.getUnShortenedUrl().isEmpty()) {
            DocumentUtils.smartOpenNewTab(this, webHead);

            // Store the last opened url
            lastOpenedUrl = webHead.getUrl();
            // If user prefers to the close the head on opening the link, then call destroySelf()
            // which will take care of closing and detaching the web head
            if (Preferences.webHeadsCloseOnOpen(WebHeadService.this)) {
                webHead.destroySelf(true);
                // Since the current url is opened, lets prepare the next set of urls
                urlOrganizer.prepareNextSetOfUrls(webHeads, lastOpenedUrl, customTabManager);
            }
            hideRemoveView();
        }
    }

    @Override
    public void onWebHeadDestroyed(@NonNull WebHead webHead, boolean isLastWebHead) {
        webHead.setMaster(false);
        webHeads.remove(webHead.getUrl());
        if (isLastWebHead) {
            RemoveWebHead.get(this).destroyAnimator(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            });
        } else {
            selectNextMaster();
            // Now that this web head is destroyed, with this web head as the reference prepare the
            // other urls
            urlOrganizer.prepareNextSetOfUrls(webHeads, webHead.getUrl(), customTabManager);
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
    public void onMasterLockedToRemove() {
        springChain2D.disableDisplacement();
    }

    @Override
    public void onMasterReleasedFromRemove() {
        springChain2D.enableDisplacement();
    }

    @Override
    public void closeAll() {
        stopSelf();
    }

    @Override
    public void onMasterLongClick() {
        final ListIterator<String> it = new ArrayList<>(webHeads.keySet()).listIterator(webHeads.size());
        ArrayList<WebSite> webSites = new ArrayList<>();
        while (it.hasPrevious()) {
            final String key = it.previous();
            final WebHead webHead = webHeads.get(key);
            if (webHead != null) {
                webSites.add(webHead.getWebsite());
            }
        }
        ContextActivityHelper.open(this, webSites);
    }


    private void closeWebHeadByUrl(@NonNull String url) {
        final WebHead webHead = webHeads.get(url);
        if (webHead != null) {
            webHead.destroySelf(true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Timber.d(newConfig.toString());
        // TODO handle webhead positions after orientations change.
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

    private void hideRemoveView() {
        RemoveWebHead.disappear();
    }

    private class WebHeadNavigationCallback extends CustomTabManager.NavigationCallback {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            // TODO Implement something useful with this callbacks
            switch (navigationEvent) {
                case TAB_SHOWN:

                    break;
                case TAB_HIDDEN:
                    // When a tab is exited, prepare the other urls.
                    urlOrganizer.prepareNextSetOfUrls(webHeads, lastOpenedUrl, customTabManager);
                    // Clear the last opened preferredUrl flag
                    lastOpenedUrl = "";
                    break;
            }
        }
    }

    private void registerReceivers() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WEBHEAD_COLOR_SET);
        filter.addAction(ACTION_REBIND_WEBHEAD_TAB_CONNECTION);
        filter.addAction(ACTION_CLOSE_WEBHEAD_BY_URL);
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, filter);
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_STOP_WEBHEAD_SERVICE));
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        unregisterReceiver(broadcastReceiver);
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

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
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