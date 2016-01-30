package arun.com.chromer.util;

import android.content.Context;
import android.content.Intent;

import arun.com.chromer.services.AppDetectService;
import arun.com.chromer.services.ScannerService;
import arun.com.chromer.services.WarmupService;
import timber.log.Timber;

/**
 * Created by Arun on 30/01/2016.
 */
public class ServicesUtil {

    private ServicesUtil() {
        throw new AssertionError("Cannot instantiate");
    }

    public static void takeCareOfServices(Context ctx) {
        if (Preferences.warmUp(ctx))
            ctx.startService(new Intent(ctx, WarmupService.class));
        else
            ctx.stopService(new Intent(ctx, WarmupService.class));

        if (Preferences.dynamicToolbarOnApp(ctx) && Preferences.dynamicToolbar(ctx)) {
            Intent appDetectService = new Intent(ctx, AppDetectService.class);
            appDetectService.putExtra(AppDetectService.CLEAR_LAST_APP, true);
            ctx.startService(appDetectService);
        } else
            ctx.stopService(new Intent(ctx, AppDetectService.class));

        try {
            if (Preferences.preFetch(ctx))
                ctx.startService(new Intent(ctx, ScannerService.class));
            else
                ctx.stopService(new Intent(ctx, ScannerService.class));
        } catch (Exception e) {
            Timber.d("Ignoring startup exception of accessibility service");
        }
    }

    public static void refreshCustomTabBindings(Context context) {
        if (WarmupService.getInstance() != null) {
            Intent warmUpService = new Intent(context, WarmupService.class);
            warmUpService.putExtra(StringConstants.SHOULD_REFRESH_BINDING, true);
            context.startService(warmUpService);
        }

        if (ScannerService.getInstance() != null) {
            Intent scanService = new Intent(context, ScannerService.class);
            scanService.putExtra(StringConstants.SHOULD_REFRESH_BINDING, true);
            context.startService(scanService);
        }
    }
}
