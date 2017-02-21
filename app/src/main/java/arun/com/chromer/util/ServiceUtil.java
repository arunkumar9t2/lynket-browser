package arun.com.chromer.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.customtabs.prefetch.ScannerService;
import arun.com.chromer.customtabs.warmup.WarmUpService;
import arun.com.chromer.shared.AppDetectService;
import arun.com.chromer.shared.Constants;

/**
 * Created by Arun on 30/01/2016.
 */
public class ServiceUtil {

    private ServiceUtil() {
        throw new AssertionError("Cannot instantiate");
    }

    public static void takeCareOfServices(@NonNull Context context) {
        if (Preferences.get(context).warmUp() && !Preferences.get(context).preFetch())
            context.startService(new Intent(context, WarmUpService.class));
        else
            context.stopService(new Intent(context, WarmUpService.class));

        if (isAppBasedToolbarColor(context) || Preferences.get(context).blacklist()) {
            Intent appDetectService = new Intent(context, AppDetectService.class);
            appDetectService.putExtra(Constants.EXTRA_KEY_CLEAR_LAST_TOP_APP, true);
            context.startService(appDetectService);
        } else
            context.stopService(new Intent(context, AppDetectService.class));

        try {
            if (Preferences.get(context).preFetch())
                context.startService(new Intent(context, ScannerService.class));
            else
                context.stopService(new Intent(context, ScannerService.class));
        } catch (Exception ignored) {
        }
    }

    public static boolean isAppBasedToolbarColor(@NonNull Context ctx) {
        return Preferences.get(ctx).dynamicToolbarOnApp() && Preferences.get(ctx).dynamicToolbar();
    }

    public static void refreshCustomTabBindings(@NonNull Context context) {
        if (WarmUpService.getInstance() != null) {
            Intent warmUpService = new Intent(context, WarmUpService.class);
            warmUpService.putExtra(Constants.EXTRA_KEY_SHOULD_REFRESH_BINDING, true);
            context.startService(warmUpService);
        }

        if (ScannerService.getInstance() != null) {
            Intent scanService = new Intent(context, ScannerService.class);
            scanService.putExtra(Constants.EXTRA_KEY_SHOULD_REFRESH_BINDING, true);
            context.startService(scanService);
        }

        Intent intent = new Intent(Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION);
        intent.putExtra(Constants.EXTRA_KEY_REBIND_WEBHEAD_CXN, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
