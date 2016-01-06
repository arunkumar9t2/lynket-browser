package arun.com.chromer.chrometabutilites;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import java.util.List;

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

        addActionButtonSecondary(ctx, url, builder);
        return builder.build();
    }

    private static void addActionButtonSecondary(Context ctx, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            try {
                Bitmap icon = drawableToBitmap(ctx.getPackageManager().getApplicationIcon(PrefUtil.getSecondaryPref(ctx)));
                String secondaryPackage = PrefUtil.getSecondaryPref(ctx);

                Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                List<ResolveInfo> resolvedActivityList = ctx.getPackageManager()
                        .queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
                for (ResolveInfo info : resolvedActivityList) {
                    if (info.activityInfo.packageName.equalsIgnoreCase(secondaryPackage))
                        activityIntent.setComponent(new ComponentName(info.activityInfo.packageName,
                                info.activityInfo.name));
                    Log.d(TAG, "Set");
                }

                PendingIntent openBrowser = PendingIntent
                        .getActivity(ctx, 0, activityIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setActionButton(icon, "Secondary browser", openBrowser);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
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

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
