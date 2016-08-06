package arun.com.chromer.webheads;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
import android.view.ViewPropertyAnimator;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import arun.com.chromer.R;
import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.webheads.helper.ColorExtractionTask;
import arun.com.chromer.webheads.physics.SpringChain2D;
import arun.com.chromer.webheads.tasks.PageExtractTasksManager;
import arun.com.chromer.webheads.ui.RemoveWebHead;
import arun.com.chromer.webheads.ui.WebHead;
import de.jetwick.snacktory.JResult;
import timber.log.Timber;

public class WebHeadService extends Service implements WebHead.WebHeadContract,
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
    /**
     * Clubbed movement manager
     */
    private final SpringChain2D mSpringChain2D = SpringChain2D.create();

    private boolean mCustomTabConnected;

    public WebHeadService() {
    }

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
        RemoveWebHead.get(this);

        // bind to custom tab session
        bindToCustomTabSession();
        registerReceivers();
        PageExtractTasksManager.registerListener(this);
    }

    public static CustomTabsSession getTabSession() {
        if (mCustomTabManager != null) {
            return mCustomTabManager.getSession();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processIntentAndWebHead(intent);

        showNotification();
        // addTestWebHeads();
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

    private void processIntentAndWebHead(Intent intent) {
        if (intent == null || intent.getDataString() == null) return; // don't do anything
        final boolean isFromNewTab = intent.getBooleanExtra(Constants.EXTRA_KEY_FROM_NEW_TAB, false);

        final String urlToLoad = intent.getDataString();
        if (!isLinkAlreadyLoaded(urlToLoad)) {
            addWebHead(urlToLoad, isFromNewTab);
        } else
            Toast.makeText(this, R.string.already_loaded, Toast.LENGTH_SHORT).show();
    }

    private void addWebHead(final String webHeadUrl, boolean isNewTab) {
        PageExtractTasksManager.startExtraction(webHeadUrl);

        final WebHead newWebHead = new WebHead(/*Service*/ this, webHeadUrl, /*listener*/ this);
        newWebHead.setFromNewTab(isNewTab);

        mSpringChain2D.setMasterSprings(newWebHead.getXSpring(), newWebHead.getYSpring());

        for (WebHead oldWebHead : mWebHeads.values()) {
            oldWebHead.setMaster(false);
            oldWebHead.setSpringConfig(SpringConfigs.ATTACHMENT);
            mSpringChain2D.addSlaveSprings(oldWebHead.getXSpring(), oldWebHead.getYSpring());
        }
        newWebHead.reveal();
        mWebHeads.put(webHeadUrl, newWebHead);
    }

    @Override
    public void onUrlUnShortened(String originalUrl, String unShortenedUrl) {
        // First check if the associated web head is active
        final WebHead webHead = mWebHeads.get(originalUrl);
        if (webHead != null) {
            webHead.setUnShortenedUrl(unShortenedUrl);
            if (mCustomTabConnected)
                mCustomTabManager.mayLaunchUrl(Uri.parse(unShortenedUrl), null, getPossibleUrls());
            else
                deferMayLaunchUntilConnected(unShortenedUrl);
        }
    }

    @Override
    public void onUrlExtracted(String originalUrl, @Nullable JResult result) {
        final WebHead webHead = mWebHeads.get(originalUrl);
        if (webHead != null && Preferences.favicons(this) && result != null) {
            try {
                final String faviconUrl = result.getFaviconUrl();
                webHead.setTitle(result.getTitle());
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

                                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                                roundedBitmapDrawable.setAntiAlias(true);
                                roundedBitmapDrawable.setCircular(true);
                                webHead.setFaviconDrawable(roundedBitmapDrawable);
                            }
                        });
            } catch (Exception e) {
                Timber.e(e.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    private void addTestWebHeads() {
        //addWebHead("http://www.medium.com");
        //addWebHead("https://www.linkedin.com/");
        addWebHead("http://www.github.com", false);
        addWebHead("http://www.androidpolice.com", false);
        // addWebHead("https://bitbucket.org/");
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
        Stack<String> urlStack = getUrlStack(sLastOpenedUrl);
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
            if (url == null) continue;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onWebHeadClick(@NonNull WebHead webHead) {
        if (webHead.getUnShortenedUrl() != null && webHead.getUnShortenedUrl().length() != 0) {
            final Intent customTabActivity = new Intent(this, CustomTabActivity.class);
            customTabActivity.setData(Uri.parse(webHead.getUnShortenedUrl()));
            customTabActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (webHead.isFromNewTab() || Preferences.mergeTabs(this)) {
                customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                customTabActivity.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            customTabActivity.putExtra(Constants.EXTRA_KEY_FROM_WEBHEAD, true);
            customTabActivity.putExtra(Constants.EXTRA_KEY_WEBHEAD_TITLE, webHead.getTitle());
            customTabActivity.putExtra(Constants.EXTRA_KEY_WEBHEAD_ICON, webHead.getFaviconBitmap());
            customTabActivity.putExtra(Constants.EXTRA_KEY_WEBHEAD_COLOR, webHead.getWebHeadColor(false));

            startActivity(customTabActivity);

            // Store the last opened url
            sLastOpenedUrl = webHead.getUrl();
            // If user prefers to the close the head on opening the link, then call destroySelf()
            // which will take care of closing and detaching the web head
            if (Preferences.webHeadsCloseOnOpen(WebHeadService.this)) {
                webHead.destroySelf(true);
            }
            // Since the current url is opened, lets prepare the next set of urls
            prepareNextSetOfUrls(sLastOpenedUrl);
            hideRemoveView();
        }
    }

    @Override
    public void onWebHeadDestroyed(@NonNull WebHead webHead, boolean isLastWebHead) {
        mWebHeads.remove(webHead.getUrl());
        if (isLastWebHead) {
            // animate remove web head before killing this service
            final ViewPropertyAnimator animator = RemoveWebHead.get(this).destroyAnimator();
            if (animator == null) {
                stopSelf();
            } else {
                animator.setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        stopSelf();
                    }
                });
                animator.start();
            }
        } else {
            // Now that this web head is destroyed, with this web head as the reference prepare the
            // other urls
            prepareNextSetOfUrls(webHead.getUrl());
        }
    }

    @Override
    public void onMasterWebHeadMoved(int x, int y) {
        mSpringChain2D.performMove(x, y);
    }

    @Override
    public Spring newSpring() {
        return mSpringSystem.createSpring();
    }

    @Override
    public void onDestroy() {
        Timber.d("Exiting webhead service");
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

    @SuppressWarnings("unused")
    private void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
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
            }
        }
    };

    private final BroadcastReceiver mStopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };
}