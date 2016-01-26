package arun.com.chromer.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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

import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import arun.com.chromer.util.Preferences;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class ScannerService extends AccessibilityService implements MyCustomActivityHelper.ConnectionCallback {

    private static ScannerService mScannerService = null;
    private MyCustomActivityHelper myCustomActivityHelper;
    private String lastWarmedUpUrl = "";

    public static ScannerService getInstance() {
        return mScannerService;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mScannerService = this;
        myCustomActivityHelper = new MyCustomActivityHelper();
        myCustomActivityHelper.setConnectionCallback(this);
        myCustomActivityHelper.setNavigationCallback(new MyCustomActivityHelper.NavigationCallback());
        boolean success = myCustomActivityHelper.bindCustomTabsService(this);
        Timber.d("Was binded " + success);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mScannerService = null;
        myCustomActivityHelper.unbindCustomTabsService(this);
        Timber.d("Unbinding");
        return super.onUnbind(intent);
    }

    public CustomTabsSession getTabSession() {
        if (myCustomActivityHelper != null) {
            return myCustomActivityHelper.getSession();
        }
        return null;
    }


    public boolean mayLaunchUrl(Uri uri, List<Bundle> possibleUrls) {
        if (!Preferences.preFetch(this)) return false;

        boolean ok = myCustomActivityHelper.mayLaunchUrl(uri, null, possibleUrls);
        Timber.d("Warmup " + ok);
        return ok;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mScannerService = this;

        if (event == null) return;

        String packageName = event.getPackageName().toString();

        if (Preferences.preFetch(this) && isWifiConditionsMet()) {
            try {
                stopService(new Intent(this, WarmupService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }

            TextProcessorTask textProcessorTask = new TextProcessorTask(getRootInActiveWindow());

            textProcessorTask.execute();
        } else {
            // Do nothing
        }
    }

    @Override
    public void onInterrupt() {
        // Nothing
    }

    @Override
    public void onCustomTabsConnected() {
        Timber.d("connected");
    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    public boolean isWifiConditionsMet() {
        if (Preferences.wifiOnlyPrefetch(this)) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            // TODO fix this deprecated call
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return mWifi.isConnected();
        } else
            return true;
    }

    private class TextProcessorTask extends AsyncTask<Void, String, Void> {
        private AccessibilityNodeInfo info;
        private Stack<AccessibilityNodeInfo> tree;
        private int maxUrl = 4;
        private int extractedCount = 0;
        private List<String> urls = new ArrayList<>();

        TextProcessorTask(AccessibilityNodeInfo nodeInfo) {
            info = nodeInfo;
            tree = new Stack<>();
        }

        void extractURL(String string) {
            if (string == null) {
                return;
            }
            Matcher m = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                    .matcher(string);
            while (m.find()) {
                String url = m.group();
                // Timber.d( "URL extracted: " + url);
                if (!url.toLowerCase().matches("^\\w+://.*")) {
                    url = "http://" + url;
                }
                urls.add(url);
            }
        }


        private void actOnCurrentNode(AccessibilityNodeInfo node) {
            if (node != null && node.getText() != null) {
                String currNodeText;
                currNodeText = node.getText().toString();
                extractURL(currNodeText);
                if (urls != null && urls.size() != 0) {
                    extractedCount += urls.size();
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (info == null) return null;

            tree.push(info);

            while (!tree.empty() && extractedCount < maxUrl) {
                AccessibilityNodeInfo currNode = tree.pop();
                if (currNode != null) {
                    actOnCurrentNode(currNode);
                    for (int i = 0; i < currNode.getChildCount(); i++) {
                        tree.push(currNode.getChild(i));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                Timber.d("Extracted " + url);
            }

            Collections.reverse(urls);

            if (urls.size() != 0) {
                int first = 0;
                String priorityUrl = null;
                List<Bundle> possibleUrls = new ArrayList<>();
                for (String url : urls) {
                    if (first == 0) {
                        priorityUrl = url;
                        first++;
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));
                        possibleUrls.add(bundle);
                    }
                    boolean success;
                    if (!priorityUrl.equalsIgnoreCase(lastWarmedUpUrl)) {
                        success = myCustomActivityHelper.mayLaunchUrl(Uri.parse(priorityUrl), null, possibleUrls);
                        if (success) lastWarmedUpUrl = priorityUrl;
                    } else {
                        Timber.d("Ignored, already warmed up");
                    }
                }
            }

            // Dispose resources after processing is done, should help with RAM usage.
            try {
                if (info != null) {
                    info.recycle();
                }
                if (tree != null) {
                    tree.clear();
                    tree = null;
                }
                if (urls != null) {
                    urls.clear();
                    urls = null;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
