package arun.com.chromer.chrometabutilites;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import arun.com.chromer.R;
import arun.com.chromer.services.ClipboardService;
import arun.com.chromer.services.ScannerService;
import arun.com.chromer.services.WarmupService;
import arun.com.chromer.util.PrefUtil;

/**
 * Created by Arun on 06/01/2016.
 */
public class CustomTabDelegate {
    private static final String TAG = CustomTabDelegate.class.getSimpleName();

    public static CustomTabsIntent getCustomizedTabIntent(Context ctx, String url) {
        CustomTabsIntent.Builder builder;

        CustomTabsSession session = getAvailableSessions(ctx);

        if (session != null) {
            builder = new CustomTabsIntent.Builder(session);
        } else
            builder = new CustomTabsIntent.Builder();


        if (PrefUtil.isColoredToolbar(ctx)) {
            final int chosenColor = PrefUtil.getToolbarColor(ctx);
            builder.setToolbarColor(chosenColor);
        }

        if (PrefUtil.isAnimationEnabled(ctx)) {
            switch (PrefUtil.getAnimationPref(ctx)) {
                case 1:
                    builder.setStartAnimations(ctx, R.anim.slide_in_right, R.anim.slide_out_left)
                            .setExitAnimations(ctx, R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case 2:
                    builder.setStartAnimations(ctx, R.anim.slide_up_right, R.anim.slide_down_left)
                            .setExitAnimations(ctx, R.anim.slide_up_left, R.anim.slide_down_right);
                    break;
                default:
                    builder.setStartAnimations(ctx, R.anim.slide_in_right, R.anim.slide_out_left)
                            .setExitAnimations(ctx, R.anim.slide_in_left, R.anim.slide_out_right);
            }

        }

        builder.setShowTitle(true);
        addShareIntent(ctx, url, builder);

        addCopyItem(ctx, url, builder);

        addShortcuttoHomescreen(ctx, url, builder);
        return builder.build();
    }

    private static CustomTabsSession getAvailableSessions(Context ctx) {
        ScannerService sService = ScannerService.getInstance();
        if (sService != null && PrefUtil.isPreFetchPrefered(ctx)) {
            Log.d(TAG, "Scanner service is running properly");
            return sService.getTabSession();
        }
        WarmupService service = WarmupService.getInstance();
        if (service != null) {
            Log.d(TAG, "Warmup service is running properly");
            return service.getTabSession();
        }
        Log.d(TAG, "No existing sessions present");
        return null;
    }

    private static void addShortcuttoHomescreen(Context c, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent addShortcutIntent = new Intent(c, AddHomeShortcutReceiver.class);
            PendingIntent addShortcut = PendingIntent
                    .getBroadcast(c, 0, addShortcutIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addMenuItem(c.getString(R.string.add_to_homescreen), addShortcut);
        }
    }

    private static void addCopyItem(Context c, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent clipboardIntent = new Intent(c, ClipboardService.class);
            PendingIntent serviceIntent = PendingIntent.getService(c, 0, clipboardIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addMenuItem(c.getString(R.string.copy_link), serviceIntent);
        }
    }

    private static void addShareIntent(Context c, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent shareIntent = new Intent(c, ShareBroadcastReceiver.class);
            PendingIntent pendingShareIntent = PendingIntent
                    .getBroadcast(c, 0, shareIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addMenuItem(c.getString(R.string.share), pendingShareIntent);
        }
    }
}
