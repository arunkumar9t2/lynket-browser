package arun.com.chromer.webheads;

import android.animation.Animator;
import android.animation.AnimatorSet;
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
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.NotificationCompat;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import arun.com.chromer.R;
import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.customtabs.CustomActivityHelper;
import arun.com.chromer.preferences.Preferences;
import arun.com.chromer.util.Constants;
import timber.log.Timber;

public class WebHeadService extends Service implements WebHead.WebHeadInteractionListener,
        CustomActivityHelper.ConnectionCallback {

    public static final String SHOULD_REBIND = "should_rebind";
    public static final String REBIND_EVENT = "rebind_event";
    private static final String CLOSE_SERVICE = "close_service";

    private static WebHeadService sInstance = null;

    private static String sLastOpenedUrl = "";

    private final LinkedHashMap<String, WebHead> mWebHeads = new LinkedHashMap<>();

    private WindowManager mWindowManager;

    private CustomActivityHelper mCustomActivityHelper;

    private final BroadcastReceiver mRebindReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean shouldRebind = intent.getBooleanExtra(SHOULD_REBIND, false);
            if (shouldRebind) bindToCustomTabSession();
        }
    };


    private final BroadcastReceiver mStopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Closing from notification");
            stopSelf();
        }
    };


    private boolean mCustomTabConnected;

    public WebHeadService() {
    }

    public static WebHeadService getInstance() {
        return sInstance;
    }

    private void initRemoveWebhead() {
        RemoveWebHead.get(this);
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

        sInstance = this;

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // create a remove view
        initRemoveWebhead();

        // bind to custom tab session
        bindToCustomTabSession();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRebindReceiver, new IntentFilter(REBIND_EVENT));

        // Register the receiver which will stop this service.
        registerReceiver(mStopServiceReceiver, new IntentFilter(CLOSE_SERVICE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processIntentAndLaunchBubble(intent);

        showNotification();

        // addTestWebHeads();
        return START_STICKY;
    }

    private void processIntentAndLaunchBubble(Intent intent) {
        if (intent == null || intent.getDataString() == null) return; // don't do anything

        String urlToLoad = intent.getDataString();

        if (!isLinkAlreadyLoaded(urlToLoad)) {
            addWebHead(urlToLoad);

            if (mCustomTabConnected)
                mCustomActivityHelper.mayLaunchUrl(Uri.parse(urlToLoad), null, getPossibleUrls());
            else
                deferMayLaunchUntilConnected(urlToLoad);
        } else
            Toast.makeText(this, "Already loaded", Toast.LENGTH_SHORT).show();
    }

    private void addWebHead(final String webHeadUrl) {
        // Before adding new web heads, call move self to stack distance on existing web heads to move
        // them a little such that they appear to be stacked
        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animators = new LinkedList<>();
        for (WebHead webhead : mWebHeads.values()) {
            Animator anim = webhead.getStackDistanceAnimator();
            if (anim != null) animators.add(anim);
        }
        animatorSet.playTogether(animators);
        animatorSet.start();

        WebHead webHead = new WebHead(WebHeadService.this, webHeadUrl);
        webHead.setWebHeadInteractionListener(WebHeadService.this);
        mWindowManager.addView(webHead, webHead.getWindowParams());
        mWebHeads.put(webHead.getUrl(), webHead);
        // beginFaviconLoading(webHead);
    }

    @SuppressWarnings("unused")
    private void beginFaviconLoading(final WebHead webHead) {
        String url;
        try {
            url = new URL(webHead.getUrl()).getHost();
            Glide.with(this)
                    .load(String.format("http://icons.better-idea.org/icon?url=%s&size=120", url))
                    .asBitmap()
                    .into(new BitmapImageViewTarget(webHead.getFaviconView()) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                            roundedBitmapDrawable.setAntiAlias(true);
                            roundedBitmapDrawable.setCircular(true);
                            webHead.setFaviconDrawable(roundedBitmapDrawable);
                        }
                    });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void showNotification() {
        PendingIntent contentIntent = PendingIntent.getBroadcast(this,
                0,
                new Intent(CLOSE_SERVICE),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chromer_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentText(getString(R.string.tap_close_all))
                .setContentTitle(getString(R.string.web_heads_service))
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .build();

        startForeground(1, notification);
    }

    @SuppressWarnings("unused")
    private void addTestWebHeads() {
        addWebHead("http://www.google.com");

        addWebHead("http://www.twitter.com");

        addWebHead("http://www.androidpolice.com");
    }

    private boolean isLinkAlreadyLoaded(String urlToLoad) {
        return urlToLoad == null || mWebHeads.containsKey(urlToLoad);
    }

    private void deferMayLaunchUntilConnected(final String urlToLoad) {
        Thread deferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 10) {
                    Timber.d("Trying to command may launch");
                    try {
                        if (mCustomTabConnected) {
                            Thread.sleep(300);
                            boolean ok = mCustomActivityHelper.mayLaunchUrl(Uri.parse(urlToLoad),
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
            boolean ok = mCustomActivityHelper.mayLaunchUrl(Uri.parse(priorityUrl), null, possibleUrls);
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
        if (mCustomActivityHelper != null) {
            // Already an instance exists, so we will un bind the current connection and then
            // bind again.
            Timber.d("Severing existing connection");
            mCustomActivityHelper.unbindCustomTabsService(this);
        }

        mCustomActivityHelper = new CustomActivityHelper();
        mCustomActivityHelper.setConnectionCallback(this);
        mCustomActivityHelper.setNavigationCallback(new WebHeadNavigationCallback());

        boolean ok = mCustomActivityHelper.bindCustomTabsService(this);
        if (ok) Timber.d("Binding successful");
    }

    private void closeAllWebHeads() {
        for (WebHead webhead : mWebHeads.values()) {
            if (webhead != null) webhead.destroySelf(false);
        }
        // Since no callback is received clear the map manually.
        mWebHeads.clear();
        Timber.d("Webheads: %d", mWebHeads.size());
    }

    @Override
    public void onWebHeadClick(@NonNull WebHead webHead) {
        if (webHead.getUrl() != null && webHead.getUrl().length() != 0) {
            Intent webHeadActivity = new Intent(this, CustomTabActivity.class);
            webHeadActivity.setData(Uri.parse(webHead.getUrl()));
            webHeadActivity.putExtra(Constants.FROM_WEBHEAD, true);
            webHeadActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(webHeadActivity);

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
    public void onWebHeadDestroy(@NonNull WebHead webHead, boolean isLastWebHead) {
        mWebHeads.remove(webHead.getUrl());

        if (isLastWebHead) {
            stopSelf();
        } else {
            // Now that this web head is destroyed, with this web head as the reference prepare the
            // other urls
            prepareNextSetOfUrls(webHead.getUrl());
        }
    }

    @Override
    public void onDestroy() {
        Timber.d("Exiting webhead service");

        closeAllWebHeads();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRebindReceiver);

        if (mCustomActivityHelper != null) mCustomActivityHelper.unbindCustomTabsService(this);

        sInstance = null;

        unregisterReceiver(mStopServiceReceiver);

        stopForeground(true);

        RemoveWebHead.destroy();

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Timber.d(newConfig.toString());
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

    public CustomTabsSession getTabSession() {
        if (mCustomActivityHelper != null) {
            return mCustomActivityHelper.getSession();
        }
        return null;
    }

    private void hideRemoveView() {
        RemoveWebHead.hideSelf();
    }


    @SuppressWarnings("unused")
    private void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private class WebHeadNavigationCallback extends CustomActivityHelper.NavigationCallback {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
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

}