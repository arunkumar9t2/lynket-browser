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
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import arun.com.chromer.util.PrefUtil;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 06/01/2016.
 */
public class ScannerService extends AccessibilityService implements MyCustomActivityHelper.ConnectionCallback {

    private static final String TAG = ScannerService.class.getSimpleName();

    private static ScannerService mScannerService = null;
    String lastWarmedUpUrl = "";
    private MyCustomActivityHelper myCustomActivityHelper;

    public static ScannerService getInstance() {
        return mScannerService;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mScannerService = this;
        myCustomActivityHelper = new MyCustomActivityHelper();
        myCustomActivityHelper.setConnectionCallback(this);
        myCustomActivityHelper.setNavigationCallback(new MyCustomActivityHelper.NavigationCallback() {
            // Do nothing
        });
        boolean success = myCustomActivityHelper.bindCustomTabsService(this);
        Log.d(TAG, "Was binded " + success);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mScannerService = null;
        myCustomActivityHelper.unbindCustomTabsService(this);
        Log.d(TAG, "Unbinding");
        return super.onUnbind(intent);
    }

    public CustomTabsSession getTabSession() {
        if (myCustomActivityHelper != null) {
            return myCustomActivityHelper.getSession();
        }
        return null;
    }


    public boolean mayLaunchUrl(Uri uri) {
        boolean ok = myCustomActivityHelper.mayLaunchUrl(uri, null, null);
        Log.d(TAG, "Warmup " + ok);
        return ok;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mScannerService = this;

        if (PrefUtil.isPreFetchPrefered(this) && shouldHonourWifi()) {
            try {
                stopService(new Intent(this, WarmupService.class));
            } catch (Exception e) {
            }

            CharSequence packageName = event.getPackageName();
            //Log.d(TAG, "Targetting " + packageName + " now");

            String texts = extractText(getRootInActiveWindow());
            List<String> urls = Util.findURLs(texts);
            Collections.reverse(urls);

            if (urls.size() != 0) {
                int first = 0;
                String priortyUrl = null;
                List<Bundle> possibleUrls = new ArrayList<>();
                for (String url : urls) {
                    if (first == 0) {
                        priortyUrl = url;
                        first++;
                        continue;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));
                    possibleUrls.add(bundle);

                    boolean success;
                    if (!priortyUrl.equalsIgnoreCase(lastWarmedUpUrl)) {
                        success = myCustomActivityHelper.mayLaunchUrl(Uri.parse(priortyUrl), null, possibleUrls);
                        if (success) lastWarmedUpUrl = priortyUrl;
                    } else {
                        Log.d(TAG, "Ignored, already warmed up");
                    }
                }
            }
        } else {
            // Do nothing
        }


    }

    @Override
    public void onInterrupt() {
        // Nothing
    }

    private String extractText(AccessibilityNodeInfo source) {
        String string = "";
        if (source == null) {
            return string;
        }
        if (source.getText() != null) {
            String text = source.getText().toString();

            text = text.replace("\n", " ");

            string += text + " ";

            // Log.d(TAG, "Text: " + text);
        }
        for (int i = 0; i < source.getChildCount(); i++) {
            string += extractText(source.getChild(i));
        }
        source.recycle();
        return string;
    }

    @Override
    public void onCustomTabsConnected() {
        Log.d(TAG, "connected");
    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    public boolean shouldHonourWifi() {
        if (PrefUtil.isWifiPreferred(this)) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return mWifi.isConnected();
        } else
            return true;
    }
}
