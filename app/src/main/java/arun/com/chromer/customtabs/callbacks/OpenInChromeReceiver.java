package arun.com.chromer.customtabs.callbacks;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.Utils;

public class OpenInChromeReceiver extends BroadcastReceiver {
    public OpenInChromeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String url = intent.getDataString();
        if (url != null) {
            final String customTabPkg = Preferences.customTabApp(context);
            if (Utils.isPackageInstalled(context, customTabPkg)) {
                final Intent chromeIntentExplicit = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                final ComponentName cN = Utils.getBrowserComponentForPackage(context, customTabPkg);
                if (cN != null) {
                    chromeIntentExplicit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    chromeIntentExplicit.setComponent(cN);
                    try {
                        context.startActivity(chromeIntentExplicit);
                    } catch (ActivityNotFoundException ignored) {

                    }
                }
            }
        }
    }
}
