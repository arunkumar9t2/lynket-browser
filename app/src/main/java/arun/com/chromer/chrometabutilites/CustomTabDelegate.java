package arun.com.chromer.chrometabutilites;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.db.AppColor;
import arun.com.chromer.db.WebColor;
import arun.com.chromer.receivers.AddHomeShortcutReceiver;
import arun.com.chromer.receivers.FavShareBroadcastReceiver;
import arun.com.chromer.receivers.SecondaryBrowserReceiver;
import arun.com.chromer.receivers.ShareBroadcastReceiver;
import arun.com.chromer.services.AppColorExtractorService;
import arun.com.chromer.services.AppDetectService;
import arun.com.chromer.services.ClipboardService;
import arun.com.chromer.services.ScannerService;
import arun.com.chromer.services.WarmupService;
import arun.com.chromer.services.WebColorExtractorService;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.Util;
import arun.com.chromer.webheads.WebHeadService;
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

        builder.setShowTitle(true);

        handleAnimations(ctx, builder);

        handleToolbarColor(ctx, url, builder);

        addShareIntent(ctx, url, builder);

        switch (Preferences.preferredAction(ctx)) {
            // TODO handle cases of uninstalling packages
            case 1:
                addActionButtonSecondary(ctx, url, builder);
                addMenuFavShareApp(ctx, url, builder);
                break;
            case 2:
                addMenuSecondaryBrowser(ctx, url, builder);
                addActionBtnFavShareApp(ctx, url, builder);
                break;
            default:
                addActionButtonSecondary(ctx, url, builder);
                addMenuFavShareApp(ctx, url, builder);
                break;
        }

        addCopyItem(ctx, url, builder);

        addShortcutToHomescreen(ctx, url, builder);

        return builder.build();
    }


    public static CustomTabsIntent getWebHeadIntent(Context ctx, String url) {

        CustomTabsIntent.Builder builder;

        if (WebHeadService.getInstance() != null && WebHeadService.getInstance().getTabSession() != null) {
            CustomTabsSession session = WebHeadService.getInstance().getTabSession();
            builder = new CustomTabsIntent.Builder(session);
        } else
            builder = new CustomTabsIntent.Builder();

        builder.setShowTitle(true);

        handleAnimations(ctx, builder);

        handleToolbarColor(ctx, url, builder);

        addShareIntent(ctx, url, builder);

        switch (Preferences.preferredAction(ctx)) {
            // TODO handle cases of uninstalling packages
            case 1:
                addActionButtonSecondary(ctx, url, builder);
                addMenuFavShareApp(ctx, url, builder);
                break;
            case 2:
                addMenuSecondaryBrowser(ctx, url, builder);
                addActionBtnFavShareApp(ctx, url, builder);
                break;
            default:
                addActionButtonSecondary(ctx, url, builder);
                addMenuFavShareApp(ctx, url, builder);
                break;
        }

        addCopyItem(ctx, url, builder);

        addShortcutToHomescreen(ctx, url, builder);

        return builder.build();
    }


    private static void handleAnimations(Context ctx, CustomTabsIntent.Builder builder) {
        if (Preferences.isAnimationEnabled(ctx)) {
            switch (Preferences.animationType(ctx)) {
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
    }

    private static void handleToolbarColor(Context ctx, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            //Toast.makeText(ctx, AppDetectService.getInstance().getLastApp(), Toast.LENGTH_SHORT).show();
            if (Preferences.isColoredToolbar(ctx)) {
                // Get the user chosen color first
                int chosenColor = Preferences.toolbarColor(ctx);

                if (Preferences.dynamicToolbar(ctx)) {

                    // Attempt to get the color of the calling app then
                    if (Preferences.dynamicToolbarOnApp(ctx)) {
                        try {
                            String lastApp = AppDetectService.getInstance().getLastApp();
                            List<AppColor> appColors = AppColor.find(AppColor.class, "app = ?", lastApp);

                            if (appColors.size() > 0) {
                                // Extracted colors exists
                                Timber.d("Using color for " + lastApp);
                                chosenColor = appColors.get(0).getColor();
                            } else {
                                // Color does not exist for this app, so let's extract it
                                doExtractionForApp(ctx, lastApp);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Further try to get extract theme color of website
                    if (Preferences.dynamicToolbarOnWeb(ctx)) {
                        // Check if we have the color extracted for this source
                        String host = Uri.parse(url).getHost();
                        if (host != null) {
                            List<WebColor> webColors = WebColor.find(WebColor.class, "url = ?", host);

                            if (webColors.size() > 0) {
                                // Extracted colors exists
                                chosenColor = webColors.get(0).getColor();
                            } else {
                                // Color does not exist for this site, so let's extract it
                                doExtractionForUrl(ctx, url);
                            }
                        }
                    }
                }
                builder.setToolbarColor(chosenColor);
            }
        }
    }

    private static void doExtractionForUrl(Context ctx, String url) {
        Intent extractorService = new Intent(ctx, WebColorExtractorService.class);
        extractorService.setData(Uri.parse(url));
        ctx.startService(extractorService);
    }

    private static void doExtractionForApp(Context ctx, String app) {
        Intent extractorService = new Intent(ctx, AppColorExtractorService.class);
        extractorService.putExtra("app", app);
        ctx.startService(extractorService);
    }

    private static void addActionButtonSecondary(Context ctx, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent activityIntent = new Intent(ctx, SecondaryBrowserReceiver.class);

            PendingIntent openBrowser = PendingIntent.getBroadcast(ctx, 0, activityIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

            String pakage = Preferences.secondaryBrowserPackage(ctx);

            if (pakage == null || !Util.isPackageInstalled(ctx, pakage)) return;

            Bitmap icon;
            try {
                icon = Util.drawableToBitmap(ctx.getPackageManager().getApplicationIcon(pakage));
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }
            builder.setActionButton(icon, "Secondary browser", openBrowser);
        }
    }

    private static void addMenuSecondaryBrowser(Context ctx, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            try {
                Intent browserInter = new Intent(ctx, SecondaryBrowserReceiver.class);

                PendingIntent openBrowser = PendingIntent.getBroadcast(ctx, 0, browserInter,
                        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

                String pakage = Preferences.secondaryBrowserPackage(ctx);

                if (pakage == null || !Util.isPackageInstalled(ctx, pakage)) return;

                String app = Util.getAppNameWithPackage(ctx, pakage);

                String label = String.format(ctx.getString(R.string.open_in_browser), app);

                builder.addMenuItem(label, openBrowser);
            } catch (Exception e) {
                Timber.d("Was not able to set secondary browser");
            }
        }
    }

    private static void addMenuFavShareApp(Context ctx, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent shareIntent = new Intent(ctx, FavShareBroadcastReceiver.class);
            PendingIntent pendingShareIntent = PendingIntent.getBroadcast(ctx, 0, shareIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

            String pakage = Preferences.favSharePackage(ctx);

            if (pakage == null || !Util.isPackageInstalled(ctx, pakage)) return;

            String app = Util.getAppNameWithPackage(ctx, pakage);

            String label = String.format(ctx.getString(R.string.share_with), app);

            builder.addMenuItem(label, pendingShareIntent);
        }
    }

    private static void addActionBtnFavShareApp(Context ctx, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent shareIntent = new Intent(ctx, FavShareBroadcastReceiver.class);
            PendingIntent pendingShareIntent = PendingIntent.getBroadcast(ctx, 0, shareIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

            String pakage = Preferences.favSharePackage(ctx);

            if (pakage == null || !Util.isPackageInstalled(ctx, pakage)) return;

            Bitmap icon;
            try {
                icon = Util.drawableToBitmap(ctx.getPackageManager().getApplicationIcon(pakage));
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }

            builder.setActionButton(icon, "Fav share app", pendingShareIntent);
        }
    }

    private static CustomTabsSession getAvailableSessions(Context ctx) {
        ScannerService sService = ScannerService.getInstance();
        if (sService != null && Preferences.preFetch(ctx)) {
            Timber.d("Using scanner service");
            return sService.getTabSession();
        }
        WarmupService service = WarmupService.getInstance();
        if (service != null) {
            Timber.d("Using warmup service");
            return service.getTabSession();
        }
        Timber.d("No existing sessions present");
        return null;
    }

    private static void addShortcutToHomescreen(Context c, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent addShortcutIntent = new Intent(c, AddHomeShortcutReceiver.class);
            PendingIntent addShortcut = PendingIntent.getBroadcast(c, 0, addShortcutIntent,
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
            PendingIntent pendingShareIntent = PendingIntent.getBroadcast(c, 0, shareIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addMenuItem(c.getString(R.string.share), pendingShareIntent);
        }
    }

}
