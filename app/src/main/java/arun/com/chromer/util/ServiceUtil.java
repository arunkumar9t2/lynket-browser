package arun.com.chromer.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import arun.com.chromer.customtabs.prefetch.ScannerService;
import arun.com.chromer.customtabs.warmup.WarmupService;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.shared.AppDetectService;
import arun.com.chromer.shared.Constants;
import timber.log.Timber;

/**
 * Created by Arun on 30/01/2016.
 */
public class ServiceUtil {

    private ServiceUtil() {
        throw new AssertionError("Cannot instantiate");
    }

    public static void takeCareOfServices(@NonNull Context context) {
        if (Preferences.warmUp(context))
            context.startService(new Intent(context, WarmupService.class));
        else
            context.stopService(new Intent(context, WarmupService.class));

        if (isAppBasedToolbarColor(context) || Preferences.blacklist(context)) {
            Intent appDetectService = new Intent(context, AppDetectService.class);
            appDetectService.putExtra(Constants.EXTRA_KEY_CLEAR_LAST_TOP_APP, true);
            context.startService(appDetectService);
        } else
            context.stopService(new Intent(context, AppDetectService.class));

        try {
            if (Preferences.preFetch(context))
                context.startService(new Intent(context, ScannerService.class));
            else
                context.stopService(new Intent(context, ScannerService.class));
        } catch (Exception e) {
            Timber.d("Ignoring startup exception of accessibility service");
        }
    }

    public static boolean isAppBasedToolbarColor(@NonNull Context ctx) {
        return Preferences.dynamicToolbarOnApp(ctx) && Preferences.dynamicToolbar(ctx);
    }

    public static void refreshCustomTabBindings(@NonNull Context context) {
        if (WarmupService.getInstance() != null) {
            Intent warmUpService = new Intent(context, WarmupService.class);
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
