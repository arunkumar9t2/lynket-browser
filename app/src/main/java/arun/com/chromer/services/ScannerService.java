package arun.com.chromer.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsSession;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.chrometabutilites.CustomActivityHelper;
import arun.com.chromer.util.Preferences;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class ScannerService extends AccessibilityService implements CustomActivityHelper.ConnectionCallback {

    private static ScannerService mScannerService = null;
    private final int MAX_URL = 4;
    private final Stack<AccessibilityNodeInfo> mTraversalTree = new Stack<>();
    private final List<String> mExtractedUrls = new ArrayList<>();
    private CustomActivityHelper mCustomActivityHelper;
    private String mLastFetchedUrl = "";
    private int mExtractedCount = 0;

    public static ScannerService getInstance() {
        return mScannerService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCustomActivityHelper != null) {
            Timber.d("Updating connections");
            mCustomActivityHelper.unbindCustomTabsService(this);
            mCustomActivityHelper = new CustomActivityHelper();
            mCustomActivityHelper.bindCustomTabsService(this);
            mCustomActivityHelper.setConnectionCallback(this);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mScannerService = this;
        mCustomActivityHelper = new CustomActivityHelper();
        mCustomActivityHelper.setConnectionCallback(this);
        boolean success = mCustomActivityHelper.bindCustomTabsService(this);
        Timber.d("Was bound " + success);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mScannerService = null;
        mCustomActivityHelper.unbindCustomTabsService(this);
        Timber.d("Unbinding");
        return super.onUnbind(intent);
    }

    public CustomTabsSession getTabSession() {
        if (mCustomActivityHelper != null) {
            return mCustomActivityHelper.getSession();
        }
        return null;
    }


    public boolean mayLaunchUrl(Uri uri, List<Bundle> possibleUrls) {
        if (!Preferences.preFetch(this)) return false;

        boolean ok = mCustomActivityHelper.mayLaunchUrl(uri, null, possibleUrls);
        Timber.d("Warmup " + ok);
        return ok;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mScannerService = this;

        mExtractedCount = 0;

        if (event == null) return;

        String packageName = event.getPackageName().toString();

        // Stop extraction once custom tab is opened.
        if (packageName.equalsIgnoreCase(Preferences.customTabApp(this))) return;

        if (Preferences.preFetch(this) && isWifiConditionsMet()) {
            stopWarmUpService();

            // Traverse the tree and find urls
            mTraversalTree.push(getRootInActiveWindow());
            while (!mTraversalTree.empty() && mExtractedCount < MAX_URL) {
                AccessibilityNodeInfo currNode = mTraversalTree.pop();
                if (currNode != null) {
                    actOnCurrentNode(currNode);
                    for (int i = 0; i < currNode.getChildCount(); i++) {
                        mTraversalTree.push(currNode.getChild(i));
                    }
                }
            }

            if (mExtractedUrls.size() != 0) {

                Collections.reverse(mExtractedUrls);

                int first = 0;
                String priorityUrl = null;
                List<Bundle> possibleUrls = new ArrayList<>();
                for (String url : mExtractedUrls) {
                    if (first == 0) {
                        priorityUrl = url;
                        first++;
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));
                        possibleUrls.add(bundle);
                    }
                    boolean success;
                    if (!priorityUrl.equalsIgnoreCase(mLastFetchedUrl)) {
                        success = mCustomActivityHelper.mayLaunchUrl(Uri.parse(priorityUrl), null, possibleUrls);
                        if (success) mLastFetchedUrl = priorityUrl;
                    } else {
                        Timber.d("Ignored, already fetched");
                    }
                }
            }

            try {
                //getRootInActiveWindow().recycle();
                mTraversalTree.clear();
                mExtractedUrls.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void actOnCurrentNode(AccessibilityNodeInfo node) {
        if (node != null && node.getText() != null) {
            String currNodeText;
            currNodeText = node.getText().toString();
            // Timber.d(currNodeText);
            extractURL(currNodeText);
            if (mExtractedUrls != null && mExtractedUrls.size() != 0) {
                mExtractedCount += mExtractedUrls.size();
            }
        }
    }


    private void extractURL(String string) {
        if (string == null) {
            return;
        }
        Matcher m = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(string);
        while (m.find()) {
            String url = m.group();
            if (!url.toLowerCase().matches("^\\w+://.*")) {
                url = "http://" + url;
            }

            //Timber.d(url);

            mExtractedUrls.add(url);
        }
    }


    private void stopWarmUpService() {
        try {
            stopService(new Intent(this, WarmupService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isWifiConditionsMet() {
        if (Preferences.wifiOnlyPrefetch(this)) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            // TODO fix this deprecated call
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return mWifi.isConnected();
        } else
            return true;
    }

    @Override
    public void onInterrupt() {
        // Nothing
    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

}
