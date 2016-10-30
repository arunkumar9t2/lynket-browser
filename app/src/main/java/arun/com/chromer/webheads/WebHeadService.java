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
import android.support.customtabs.CustomTabsService;
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
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import arun.com.chromer.R;
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.webheads.helper.ColorExtractionTask;
import arun.com.chromer.webheads.helper.WebSite;
import arun.com.chromer.webheads.physics.SpringChain2D;
import arun.com.chromer.webheads.tasks.PageExtractTasksManager;
import arun.com.chromer.webheads.ui.RemoveWebHead;
import arun.com.chromer.webheads.ui.WebHead;
import arun.com.chromer.webheads.ui.WebHeadContract;
import arun.com.chromer.webheads.ui.context.WebHeadContextActivity;
import de.jetwick.snacktory.JResult;
import timber.log.Timber;

public class WebHeadService extends Service implements WebHeadContract,
        CustomTabManager.ConnectionCallback, PageExtractTasksManager.ProgressListener {
    private static String sLastOpenedUrl = "";
    /**
     * Connection manager instance to connect and warm up custom tab providers
     */
    private static CustomTabManager mCustomTabManager;
    /**
     * Reference to all the web heads created on screen. Ordered in the order of creation by using
     * {@link LinkedHashMap}. The key must be unique and is usually the url the web head represents.
     */
    private final Map<String, WebHead> mWebHeads = new LinkedHashMap<>();
    /**
     * The base spring system to create our springs.
     */
    private final SpringSystem mSpringSystem = SpringSystem.create();
    // Clubbed movement manager
    private SpringChain2D mSpringChain2D;
    private boolean mCustomTabConnected;
    // Max visible web heads is set 6 for performance reasons.
    public static final int MAX_VISIBLE_WEB_HEADS = 5;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Timber.d("Exited webhead service since overlay permission was revoked");
                stopSelf();
                return;
            }
        }
        mSpringChain2D = SpringChain2D.create(this);
        RemoveWebHead.get(this);

        // bind to custom tab session
        bindToCustomTabSession();
        registerReceivers();
        PageExtractTasksManager.registerListener(this);

        showNotification();
    }

    @Override
    public void onDestroy() {
        Timber.d("Exiting webhead service");
        WebHead.clearMasterPosition();
        WebHead.cancelToast();

        removeWebHeads();

        PageExtractTasksManager.cancelAll(true);
        PageExtractTasksManager.unRegisterListener();

        unregisterReceivers();

        if (mCustomTabManager != null) {
            mCustomTabManager.unbindCustomTabsService(this);
        }

        stopForeground(true);
        RemoveWebHead.destroy();
        super.onDestroy();
    }

    public static CustomTabsSession getTabSession() {
        if (mCustomTabManager != null) {
            return mCustomTabManager.getSession();
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
                new Intent(Constants.ACTION_STOP_WEBHEAD_SERVICE),
                PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chromer_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentText(getString(R.string.tap_close_all))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(getString(R.string.web_heads_service))
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .build();

        startForeground(1, notification);
    }

    private void processIntent(Intent intent) {
        if (intent == null || intent.getDataString() == null) return; // don't do anything
        final boolean isFromNewTab = intent.getBooleanExtra(Constants.EXTRA_KEY_FROM_NEW_TAB, false);
        final boolean isMinimized = intent.getBooleanExtra(Constants.EXTRA_KEY_MINIMIZE, false);

        final String urlToLoad = intent.getDataString();
        if (!isLinkAlreadyLoaded(urlToLoad)) {
            addWebHead(urlToLoad, isFromNewTab, isMinimized);
        } else if (!isMinimized) {
            Toast.makeText(this, R.string.already_loaded, Toast.LENGTH_SHORT).show();
        }
    }

    private void addWebHead(final String webHeadUrl, final boolean isNewTab, final boolean isMinimized) {
        PageExtractTasksManager.startExtraction(webHeadUrl);
        mSpringChain2D.clear();

        final WebHead newWebHead = new WebHead(/*Service*/ this, webHeadUrl, /*listener*/ this);
        newWebHead.setFromNewTab(isNewTab);

        mSpringChain2D.setMasterSprings(newWebHead.getXSpring(), newWebHead.getYSpring());

        int index = mWebHeads.values().size();
        for (WebHead oldWebHead : mWebHeads.values()) {
            oldWebHead.setMaster(false);
            if (shouldQueue(index + 1)) {
                oldWebHead.setInQueue(true);
            } else {
                oldWebHead.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(90, 9 + (index * 5)));
                mSpringChain2D.addSlaveSprings(oldWebHead.getXSpring(), oldWebHead.getYSpring());
            }
            index--;
        }
        mSpringChain2D.rest();

        newWebHead.reveal();
        mWebHeads.put(webHeadUrl, newWebHead);

        if (Preferences.aggressiveLoading(this) && !isMinimized) {
            DocumentUtils.openNewCustomTab(this, newWebHead);
        }
    }

    private boolean shouldQueue(int index) {
        return index > MAX_VISIBLE_WEB_HEADS;
    }

    @Override
    public void onUrlUnShortened(String originalUrl, String unShortenedUrl) {
        // First check if the associated web head is active
        final WebHead webHead = mWebHeads.get(originalUrl);
        if (webHead != null) {
            webHead.setUnShortenedUrl(unShortenedUrl);
            if (!Preferences.aggressiveLoading(this)) {
                if (mCustomTabConnected)
                    mCustomTabManager.mayLaunchUrl(Uri.parse(unShortenedUrl), null, getPossibleUrls());
                else
                    deferMayLaunchUntilConnected(unShortenedUrl);
            }
        }
    }

    @Override
    public void onUrlExtracted(String originalUrl, @Nullable JResult result) {
        final WebHead webHead = mWebHeads.get(originalUrl);
        if (webHead != null && result != null) {
            try {
                final String faviconUrl = result.getFaviconUrl();
                webHead.setTitle(result.getTitle());
                webHead.setFaviconUrl(faviconUrl);
                Glide.with(this)
                        .load(faviconUrl)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(webHead.getFaviconView()) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (resource == null) {
                                    return;
                                }
                                // dispatch color extraction task
                                new ColorExtractionTask(webHead, resource).execute();

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
        }
    }

    private boolean isLinkAlreadyLoaded(@Nullable String urlToLoad) {
        return urlToLoad == null || mWebHeads.containsKey(urlToLoad);
    }

    private void deferMayLaunchUntilConnected(final String urlToLoad) {
        final Thread deferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 10) {
                    try {
                        if (mCustomTabConnected) {
                            Thread.sleep(300);
                            boolean ok = mCustomTabManager.mayLaunchUrl(Uri.parse(urlToLoad),
                                    null,
                                    getPossibleUrls());
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

    /**
     * Pre-fetches next set of urls for launch with the given parameter url as reference. The first
     * url in web heads order found is considered as the priority url and other url as likely urls.
     *
     * @param sLastOpenedUrl The last opened url
     */
    private void prepareNextSetOfUrls(String sLastOpenedUrl) {
        if (Preferences.aggressiveLoading(this)) return;

        final Stack<String> urlStack = getUrlStack(sLastOpenedUrl);
        if (urlStack.size() > 0) {
            String priorityUrl = urlStack.pop();
            if (priorityUrl == null) return;

            Timber.d("Priority : %s", priorityUrl);

            List<Bundle> possibleUrls = new ArrayList<>();

            for (String url : urlStack) {
                if (url == null) continue;

                Bundle bundle = new Bundle();
                bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));
                possibleUrls.add(bundle);
                Timber.d("Others : %s", url);
            }
            // Now let's prepare urls
            boolean ok = mCustomTabManager.mayLaunchUrl(Uri.parse(priorityUrl), null, possibleUrls);
            Timber.d("May launch was %b", ok);
        }
    }

    private List<Bundle> getPossibleUrls() {
        List<Bundle> possibleUrls = new ArrayList<>();
        for (WebHead webHead : mWebHeads.values()) {
            String url = webHead.getUrl();

            Bundle bundle = new Bundle();
            bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));
            possibleUrls.add(bundle);
        }
        return possibleUrls;
    }

    private Stack<String> getUrlStack(String sLastOpenedUrl) {
        Stack<String> urlStack = new Stack<>();
        if (mWebHeads.containsKey(sLastOpenedUrl)) {
            boolean foundWebHead = false;
            for (WebHead webhead : mWebHeads.values()) {
                if (!foundWebHead) {
                    foundWebHead = webhead.getUrl().equalsIgnoreCase(sLastOpenedUrl);
                    if (!foundWebHead) urlStack.push(webhead.getUrl());
                } else {
                    urlStack.push(webhead.getUrl());
                }
            }
        } else {
            for (WebHead webhead : mWebHeads.values()) {
                urlStack.push(webhead.getUrl());
            }
        }
        return urlStack;
    }

    private void bindToCustomTabSession() {
        if (mCustomTabManager != null) {
            // Already an instance exists, so we will un bind the current connection and then
            // bind again.
            Timber.d("Severing existing connection");
            mCustomTabManager.unbindCustomTabsService(this);
        }

        mCustomTabManager = new CustomTabManager();
        mCustomTabManager.setConnectionCallback(this);
        mCustomTabManager.setNavigationCallback(new WebHeadNavigationCallback());

        boolean ok = mCustomTabManager.bindCustomTabsService(this);
        if (ok) Timber.d("Binding successful");
    }

    private void removeWebHeads() {
        for (WebHead webhead : mWebHeads.values()) {
            if (webhead != null) webhead.destroySelf(false);
        }
        // Since no callback is received clear the map manually.
        mWebHeads.clear();
        Timber.d("WebHeads: %d", mWebHeads.size());
    }

    private void updateWebHeadColors(@ColorInt int webHeadColor) {
        final AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animators = new LinkedList<>();
        for (WebHead webhead : mWebHeads.values()) {
            animators.add(webhead.getRevealAnimator(webHeadColor));
        }
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    private void updateSpringChain() {
        mSpringChain2D.rest();
        mSpringChain2D.clear();
        mSpringChain2D.enableDisplacement();
        // Index that is used to differentiate spring config
        int springChainIndex = mWebHeads.values().size();
        // Index that is used to determine if the web hed should be in queue.
        int index = mWebHeads.values().size();
        for (WebHead webHead : mWebHeads.values()) {
            if (webHead != null) {
                if (webHead.isMaster()) {
                    // Master will never be in queue, so no check is made.
                    mSpringChain2D.setMasterSprings(webHead.getXSpring(), webHead.getYSpring());
                } else {
                    if (shouldQueue(index)) {
                        webHead.setInQueue(true);
                    } else {
                        webHead.setInQueue(false);
                        // We should add the springs to our chain only if the web head is active
                        webHead.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(90, 9 + (springChainIndex * 5)));
                        mSpringChain2D.addSlaveSprings(webHead.getXSpring(), webHead.getYSpring());
                    }
                    springChainIndex--;
                }
                index--;
            }
        }
    }

    private void selectNextMaster() {
        final ListIterator<String> it = new ArrayList<>(mWebHeads.keySet()).listIterator(mWebHeads.size());
        //noinspection LoopStatementThatDoesntLoop
        while (it.hasPrevious()) {
            final String key = it.previous();
            final WebHead toBeMaster = mWebHeads.get(key);
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
        if (webHead.getUnShortenedUrl().length() != 0) {

            DocumentUtils.smartOpenNewTab(this, webHead);

            // Store the last opened url
            sLastOpenedUrl = webHead.getUrl();
            // If user prefers to the close the head on opening the link, then call destroySelf()
            // which will take care of closing and detaching the web head
            if (Preferences.webHeadsCloseOnOpen(WebHeadService.this)) {
                webHead.destroySelf(true);
                // Since the current url is opened, lets prepare the next set of urls
                prepareNextSetOfUrls(sLastOpenedUrl);
            }
            hideRemoveView();
        }
    }

    @Override
    public void onWebHeadDestroyed(@NonNull WebHead webHead, boolean isLastWebHead) {
        webHead.setMaster(false);
        mWebHeads.remove(webHead.getUrl());

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
            prepareNextSetOfUrls(webHead.getUrl());
        }

        ContextActivityHelper.signalDeleted(this, webHead.getWebsite());
    }

    @Override
    public void onMasterWebHeadMoved(int x, int y) {
        mSpringChain2D.performGroupMove(x, y);
    }

    @NonNull
    @Override
    public Spring newSpring() {
        return mSpringSystem.createSpring();
    }

    @Override
    public void onMasterLockedToRemove() {
        mSpringChain2D.disableDisplacement();
    }

    @Override
    public void onMasterReleasedFromRemove() {
        mSpringChain2D.enableDisplacement();
    }

    @Override
    public void closeAll() {
        stopSelf();
    }

    @Override
    public void onMasterLongClick() {
        final ListIterator<String> it = new ArrayList<>(mWebHeads.keySet()).listIterator(mWebHeads.size());
        ArrayList<WebSite> webSites = new ArrayList<>();
        while (it.hasPrevious()) {
            final String key = it.previous();
            final WebHead webHead = mWebHeads.get(key);
            if (webHead != null) {
                webSites.add(webHead.getWebsite());
            }
        }
        ContextActivityHelper.open(this, webSites);
    }


    private void closeWebHeadByUrl(String url) {
        final WebHead webHead = mWebHeads.get(url);
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
        mCustomTabConnected = true;
        Timber.d("Connected to custom tabs successfully");
    }

    @Override
    public void onCustomTabsDisconnected() {
        mCustomTabConnected = false;
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
                    prepareNextSetOfUrls(sLastOpenedUrl);
                    // Clear the last opened url flag
                    sLastOpenedUrl = "";
                    break;
            }
        }
    }

    private void registerReceivers() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_WEBHEAD_COLOR_SET);
        filter.addAction(Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION);
        filter.addAction(Constants.ACTION_CLOSE_WEBHEAD_BY_URL);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, filter);
        registerReceiver(mStopServiceReceiver, new IntentFilter(Constants.ACTION_STOP_WEBHEAD_SERVICE));
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
        unregisterReceiver(mStopServiceReceiver);
    }

    private final BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION:
                    final boolean shouldRebind = intent.getBooleanExtra(Constants.EXTRA_KEY_REBIND_WEBHEAD_CXN, false);
                    if (shouldRebind) {
                        bindToCustomTabSession();
                    }
                    break;
                case Constants.ACTION_WEBHEAD_COLOR_SET:
                    final int webHeadColor = intent.getIntExtra(Constants.EXTRA_KEY_WEBHEAD_COLOR, Constants.NO_COLOR);
                    if (webHeadColor != Constants.NO_COLOR) {
                        updateWebHeadColors(webHeadColor);
                    }
                    break;
                case Constants.ACTION_CLOSE_WEBHEAD_BY_URL:
                    final WebSite webSite = intent.getParcelableExtra(Constants.EXTRA_KEY_WEBSITE);
                    if (webSite != null) {
                        closeWebHeadByUrl(webSite.url);
                    }
                    break;
            }
        }
    };

    private final BroadcastReceiver mStopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };


    private static class ContextActivityHelper {
        static void signalUpdated(Context context, WebSite webSite) {
            final Intent intent = new Intent(Constants.ACTION_EVENT_WEBSITE_UPDATED);
            intent.putExtra(Constants.EXTRA_KEY_WEBSITE, webSite);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        static void signalDeleted(Context context, WebSite webSite) {
            final Intent intent = new Intent(Constants.ACTION_EVENT_WEBHEAD_DELETED);
            intent.putExtra(Constants.EXTRA_KEY_WEBSITE, webSite);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        static void open(Context context, ArrayList<WebSite> webSites) {
            final Intent intent = new Intent(context, WebHeadContextActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putParcelableArrayListExtra(Constants.EXTRA_KEY_WEBSITE, webSites);
            context.startActivity(intent);
        }
    }
}