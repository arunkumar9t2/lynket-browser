package arun.com.chromer.webheads;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.NotificationCompat;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import arun.com.chromer.R;
import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.customtabs.CustomTabBindingHelper;
import arun.com.chromer.preferences.Preferences;
import arun.com.chromer.util.Constants;
import arun.com.chromer.webheads.tasks.ExtractionTasksManager;
import arun.com.chromer.webheads.ui.RemoveWebHead;
import arun.com.chromer.webheads.ui.WebHead;
import de.jetwick.snacktory.JResult;
import timber.log.Timber;

public class WebHeadService extends Service implements WebHead.WebHeadInteractionListener,
        CustomTabBindingHelper.ConnectionCallback, ExtractionTasksManager.ProgressListener {

    private static WebHeadService sInstance = null;
    private static String sLastOpenedUrl = "";

    private final Map<String, WebHead> mWebHeads = new LinkedHashMap<>();
    private boolean mCustomTabConnected;
    private WindowManager mWindowManager;
    private CustomTabBindingHelper mCustomTabBindingHelper;

    public WebHeadService() {
    }

    private final BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION:
                    boolean shouldRebind = intent.getBooleanExtra(Constants.EXTRA_KEY_REBIND_WEBHEAD_CXN, false);
                    if (shouldRebind) bindToCustomTabSession();
                    break;
                case Constants.ACTION_WEBHEAD_COLOR_SET:
                    // Update web heads colors
                    int webHeadColor = intent.getIntExtra(Constants.EXTRA_KEY_WEBHEAD_COLOR, 0);
                    if (webHeadColor != 0) {
                        updateWebHeadColors(webHeadColor);
                    }
                    break;
            }
        }
    };

    private final BroadcastReceiver mStopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Closing from notification");
            stopSelf();
        }
    };

    public static WebHeadService getInstance() {
        return sInstance;
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

        registerReceivers();

        ExtractionTasksManager.registerListener(this);
    }

    private void registerReceivers() {
        IntentFilter localIntentFilter = new IntentFilter(Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION);
        localIntentFilter.addAction(Constants.ACTION_WEBHEAD_COLOR_SET);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, localIntentFilter);

        // Register the receiver which will stop this service.
        registerReceiver(mStopServiceReceiver, new IntentFilter(Constants.ACTION_STOP_WEBHEAD_SERVICE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processIntentAndWebHead(intent);

        showNotification();
        // addTestWebHeads();
        return START_STICKY;
    }

    private void processIntentAndWebHead(Intent intent) {
        if (intent == null || intent.getDataString() == null) return; // don't do anything

        String urlToLoad = intent.getDataString();
        if (!isLinkAlreadyLoaded(urlToLoad)) {
            addWebHead(urlToLoad);
        } else
            Toast.makeText(this, R.string.already_loaded, Toast.LENGTH_SHORT).show();
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
        mWebHeads.put(webHeadUrl, webHead);


        ExtractionTasksManager.startDownload(webHeadUrl);
    }

    @Override
    public void onUrlUnShortened(String originalUrl, String unShortenedUrl) {
        // First check if the associated web head is active
        WebHead webHead = mWebHeads.get(originalUrl);

        if (webHead != null) {
            if (mCustomTabConnected)
                mCustomTabBindingHelper.mayLaunchUrl(Uri.parse(unShortenedUrl), null, getPossibleUrls());
            else
                deferMayLaunchUntilConnected(unShortenedUrl);
        }
    }

    @Override
    public void onUrlExtracted(String originalUrl, JResult result) {
        final WebHead webHead = mWebHeads.get(originalUrl);
        if (webHead != null && (Preferences.favicons(this))) {
            String faviconUrl = result.getFaviconUrl();
            try {
                Glide.with(this)
                        .load(faviconUrl)
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
            } catch (Exception e) {
                Timber.e(e.getMessage());
            }
        }
    }

    private void showNotification() {
        PendingIntent contentIntent = PendingIntent.getBroadcast(this,
                0,
                new Intent(Constants.ACTION_STOP_WEBHEAD_SERVICE),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
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
                    try {
                        if (mCustomTabConnected) {
                            Thread.sleep(300);
                            boolean ok = mCustomTabBindingHelper.mayLaunchUrl(Uri.parse(urlToLoad),
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
            boolean ok = mCustomTabBindingHelper.mayLaunchUrl(Uri.parse(priorityUrl), null, possibleUrls);
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
        if (mCustomTabBindingHelper != null) {
            // Already an instance exists, so we will un bind the current connection and then
            // bind again.
            Timber.d("Severing existing connection");
            mCustomTabBindingHelper.unbindCustomTabsService(this);
        }

        mCustomTabBindingHelper = new CustomTabBindingHelper();
        mCustomTabBindingHelper.setConnectionCallback(this);
        mCustomTabBindingHelper.setNavigationCallback(new WebHeadNavigationCallback());

        boolean ok = mCustomTabBindingHelper.bindCustomTabsService(this);
        if (ok) Timber.d("Binding successful");
    }

    private void destroyAllWebHeads() {
        for (WebHead webhead : mWebHeads.values()) {
            if (webhead != null) webhead.destroySelf(false);
        }
        // Since no callback is received clear the map manually.
        mWebHeads.clear();
        Timber.d("Webheads: %d", mWebHeads.size());
    }

    private void updateWebHeadColors(@ColorInt int webHeadColor) {
        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animators = new LinkedList<>();
        for (WebHead webhead : mWebHeads.values()) {
            Animator anim = webhead.getColorChangeAnimator(webHeadColor);
            if (anim != null) animators.add(anim);
        }
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    @Override
    public void onWebHeadClick(@NonNull WebHead webHead) {
        if (webHead.getUrl() != null && webHead.getUrl().length() != 0) {
            Intent customTabActivity = new Intent(this, CustomTabActivity.class);
            customTabActivity.setData(Uri.parse(webHead.getUrl()));
            customTabActivity.putExtra(Constants.EXTRA_KEY_FROM_WEBHEAD, true);
            customTabActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
    public void onWebHeadDestroy(@NonNull WebHead webHead, boolean isLastWebHead) {
        mWebHeads.remove(webHead.getUrl());

        if (isLastWebHead) {
            // animate remove web head before killing this service
            ViewPropertyAnimator animator = RemoveWebHead.get(this).destroyAnimator();
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
    public void onDestroy() {
        Timber.d("Exiting webhead service");

        destroyAllWebHeads();

        ExtractionTasksManager.cancelAll();
        ExtractionTasksManager.unRegisterListener();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
        unregisterReceiver(mStopServiceReceiver);

        if (mCustomTabBindingHelper != null) mCustomTabBindingHelper.unbindCustomTabsService(this);

        sInstance = null;

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

    public CustomTabsSession getTabSession() {
        if (mCustomTabBindingHelper != null) {
            return mCustomTabBindingHelper.getSession();
        }
        return null;
    }

    private void initRemoveWebhead() {
        RemoveWebHead.get(this);
    }

    private void hideRemoveView() {
        RemoveWebHead.disappear();
    }

    @SuppressWarnings("unused")
    private void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private class WebHeadNavigationCallback extends CustomTabBindingHelper.NavigationCallback {
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

}