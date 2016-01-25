package arun.com.chromer.chrometabutilites;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.model.WebColor;
import arun.com.chromer.services.ClipboardService;
import arun.com.chromer.services.ColorExtractor;
import arun.com.chromer.services.ScannerService;
import arun.com.chromer.services.WarmupService;
import arun.com.chromer.util.PrefUtil;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class CustomTabDelegate {
    public static CustomTabsIntent getCustomizedTabIntent(Context ctx, String url) {

        CustomTabsIntent.Builder builder;

        CustomTabsSession session = getAvailableSessions(ctx);

        if (session != null) {
            builder = new CustomTabsIntent.Builder(session);
        } else
            builder = new CustomTabsIntent.Builder();


        if (PrefUtil.isColoredToolbar(ctx)) {
            int chosenColor = PrefUtil.getToolbarColor(ctx);
            if (PrefUtil.isDynamicToolbar(ctx)) {
                // Check if we have the color extracted for this source
                String host = Uri.parse(url).getHost();
                List<WebColor> webColors = WebColor.find(WebColor.class, "url = ?", host);

                if (webColors.size() > 0) {
                    // Extracted colors exists
                    chosenColor = webColors.get(0).getColor();
                    Timber.d(String.valueOf(chosenColor));
                } else {
                    // Color does not exist for this site, so let's extract it
                    doExtractionForHost(ctx, url);
                }
            }
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

    private static void doExtractionForHost(Context ctx, String url) {
        Intent extractorService = new Intent(ctx, ColorExtractor.class);
        extractorService.setData(Uri.parse(url));
        ctx.startService(extractorService);
    }

    private static void addActionButtonSecondary(Context ctx, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            try {
                Bitmap icon = drawableToBitmap(ctx.getPackageManager().getApplicationIcon(PrefUtil.getSecondaryPref(ctx)));

                Intent activityIntent = new Intent(ctx, SecondaryBrowserReceiver.class);

                PendingIntent openBrowser = PendingIntent
                        .getBroadcast(ctx, 0, activityIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setActionButton(icon, "Secondary browser", openBrowser);
            } catch (PackageManager.NameNotFoundException e) {
                Timber.d("Was not able to set secondary browser");
            }
        }
    }

    private static CustomTabsSession getAvailableSessions(Context ctx) {
        ScannerService sService = ScannerService.getInstance();
        if (sService != null && PrefUtil.isPreFetchPrefered(ctx)) {
            Timber.d("Scanner service is running properly");
            return sService.getTabSession();
        }
        WarmupService service = WarmupService.getInstance();
        if (service != null) {
            Timber.d("Warmup service is running properly");
            return service.getTabSession();
        }
        Timber.d("No existing sessions present");
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

    private static Bitmap drawableToBitmap(Drawable drawable) {
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
