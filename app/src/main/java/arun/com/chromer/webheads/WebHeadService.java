package arun.com.chromer.webheads;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.activities.WebHeadActivity;
import arun.com.chromer.chrometabutilites.CustomActivityHelper;
import arun.com.chromer.util.Preferences;
import timber.log.Timber;

public class WebHeadService extends Service implements WebHead.WebHeadInteractionListener,
        CustomActivityHelper.ConnectionCallback {

    public static final String SHOULD_REBIND = "should_rebind";
    public static final String REBIND_EVENT = "rebind_event";

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

    private boolean mCustomTabConnected;

    private RemoveWebHead mRemoveWebHead;

    public WebHeadService() {
    }

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
        sInstance = this;

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mRemoveWebHead = RemoveWebHead.get(this, mWindowManager);
        mWindowManager.addView(mRemoveWebHead, mRemoveWebHead.getWindowParams());

        // bind to custom tab session
        bindToCustomTabSession();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRebindReceiver,
                new IntentFilter(REBIND_EVENT));
    }

    private void addWebHead(WebHead webHead) {
        // Before adding new web heads, call move self to stack distance on existing web heads to move
        // them a little such that they appear to be stacked
        stackPreviousWebHeads();

        mWindowManager.addView(webHead, webHead.getWindowParams());
        mWebHeads.put(webHead.getUrl(), webHead);
    }

    private void stackPreviousWebHeads() {
        for (WebHead webhead : mWebHeads.values()) {
            webhead.moveSelfToStackDistance();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        processIntentAndLaunchBubble(intent);
        //addTestWebHeads();
        return START_STICKY;
    }

    private void addTestWebHeads() {
        WebHead webHead = new WebHead(this, "http://www.google.com", mWindowManager);
        webHead.setWebHeadInteractionListener(this);
        addWebHead(webHead);

        webHead = new WebHead(this, "http://www.twitter.com", mWindowManager);
        webHead.setWebHeadInteractionListener(this);
        addWebHead(webHead);

        webHead = new WebHead(this, "http://www.androidpolice.com", mWindowManager);
        webHead.setWebHeadInteractionListener(this);
        addWebHead(webHead);
    }

    private void processIntentAndLaunchBubble(Intent intent) {
        if (intent == null) return; // don't do anything

        String urlToLoad = intent.getDataString();

        if (!isLinkAlreadyLoaded(urlToLoad)) {
            WebHead webHead = new WebHead(this, urlToLoad, mWindowManager);
            webHead.setWebHeadInteractionListener(this);
            addWebHead(webHead);

            if (mCustomTabConnected)
                mCustomActivityHelper.mayLaunchUrl(Uri.parse(urlToLoad), null, null);
            else
                deferMayLaunchUntilConnected(urlToLoad);
        } else
            Toast.makeText(this, "Already loaded", Toast.LENGTH_SHORT).show();
    }

    private boolean isLinkAlreadyLoaded(String urlToLoad) {
        if (urlToLoad == null) return true;
        return mWebHeads != null && mWebHeads.containsKey(urlToLoad);
    }

    private void deferMayLaunchUntilConnected(final String urlToLoad) {
        Thread deferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (mCustomTabConnected) {
                            Thread.sleep(300);
                            boolean ok = mCustomActivityHelper.mayLaunchUrl(Uri.parse(urlToLoad),
                                    null,
                                    null);
                            Timber.d("Deferred may launch was %b", ok);
                            break;
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        deferThread.start();
    }

    /**
     * Pre-fetches next set of urls for launch with the given parameter url as reference. The first
     * url in web heads order found is considered as the priority url and other url as likely urls.
     *
     * @param sLastOpenedUrl
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
            mCustomActivityHelper.mayLaunchUrl(Uri.parse(priorityUrl), null, possibleUrls);
        }
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
        mCustomActivityHelper.setNavigationCallback(new CustomTabNavigationCallback());

        boolean ok = mCustomActivityHelper.bindCustomTabsService(this);
        if (ok) Timber.d("Binding successful");
    }

    @Override
    public void onDestroy() {
        Timber.d("Exiting webhead service");

        if (BuildConfig.DEBUG) {
            // Toast.makeText(this, "Exited service", Toast.LENGTH_SHORT).show();
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRebindReceiver);
        if (mCustomActivityHelper != null) mCustomActivityHelper.unbindCustomTabsService(this);

        RemoveWebHead.destroy();
        mRemoveWebHead = null;

        sInstance = null;
        super.onDestroy();
    }

    @Override
    public void onWebHeadClick(WebHead webHead) {
        if (webHead.getUrl() != null && webHead.getUrl().length() != 0) {
            Intent webHeadActivity = new Intent(this, WebHeadActivity.class);
            webHeadActivity.setData(Uri.parse(webHead.getUrl()));
            webHeadActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(webHeadActivity);

            // Store the last opened url
            sLastOpenedUrl = webHead.getUrl();

            // If user prefers to the close the head on opening the link, then call destroySelf()
            // which will take care of closing and detaching the web head
            if (Preferences.webHeadsCloseOnOpen(WebHeadService.this)) {
                webHead.destroySelf();
            }

            // Since the current url is opened, lets prepare the next set of urls
            prepareNextSetOfUrls(sLastOpenedUrl);

            hideRemoveView();
        }
    }

    @Override
    public void onWebHeadDestroy(WebHead webHead, boolean isLastWebHead) {
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

    private void dimAllWebHeads() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (WebHead webhead : mWebHeads.values()) {
                    webhead.dim();
                }
            }
        });
    }

    private void brightAllWebHeads() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (WebHead webhead : mWebHeads.values()) {
                    webhead.bright();
                }
            }
        });
    }


    private void hideRemoveView() {
        if (mRemoveWebHead != null) mRemoveWebHead.hide();
    }


    private void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private class CustomTabNavigationCallback extends CustomActivityHelper.NavigationCallback {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            switch (navigationEvent) {
                case TAB_SHOWN:
                    // Edge cases where the remove view can be still visible after touching the webhead
                    hideRemoveView();

                    dimAllWebHeads();

                    break;
                case TAB_HIDDEN:
                    brightAllWebHeads();

                    // Clear the last opened url flag
                    sLastOpenedUrl = "";
                    break;
            }
        }

    }

}