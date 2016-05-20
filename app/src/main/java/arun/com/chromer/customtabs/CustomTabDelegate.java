package arun.com.chromer.customtabs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.customtabs.callbacks.AddHomeShortcutService;
import arun.com.chromer.customtabs.callbacks.ClipboardService;
import arun.com.chromer.customtabs.callbacks.FavShareBroadcastReceiver;
import arun.com.chromer.customtabs.callbacks.OpenInChromeReceiver;
import arun.com.chromer.customtabs.callbacks.OpenInNewTabReceiver;
import arun.com.chromer.customtabs.callbacks.SecondaryBrowserReceiver;
import arun.com.chromer.customtabs.callbacks.ShareBroadcastReceiver;
import arun.com.chromer.customtabs.prefetch.ScannerService;
import arun.com.chromer.customtabs.warmup.WarmupService;
import arun.com.chromer.db.AppColor;
import arun.com.chromer.db.WebColor;
import arun.com.chromer.dynamictoolbar.AppColorExtractorService;
import arun.com.chromer.dynamictoolbar.WebColorExtractorService;
import arun.com.chromer.preferences.Preferences;
import arun.com.chromer.services.AppDetectService;
import arun.com.chromer.util.Util;
import arun.com.chromer.webheads.WebHeadService;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
// TODO REWRITE THIS UGLY CODE :'(
public class CustomTabDelegate {

    private static final int OPEN_TAB = 1;
    private static final int SHARE_TAB = 2;
    private static int sToolbarColor;

    public static CustomTabsIntent getCustomizedTabIntent(@NonNull Context ctx, @NonNull String url, boolean isWebhead) {
        final CustomTabsSession session = getAvailableSessions(ctx, isWebhead);

        final CustomTabsIntent.Builder builder;
        if (session != null) {
            builder = new CustomTabsIntent.Builder(session);
        } else {
            builder = new CustomTabsIntent.Builder();
        }

        builder.setShowTitle(true);
        builder.enableUrlBarHiding();

        addAnimations(ctx, builder);

        setToolbarColor(ctx, url, builder);

        addShareIntent(ctx, builder);

        switch (Preferences.preferredAction(ctx)) {
            // TODO handle cases of uninstalling packages
            case Preferences.PREFERRED_ACTION_BROWSER:
                addActionButtonSecondaryBrowser(ctx, builder);
                addMenuFavShareApp(ctx, builder);
                break;
            case Preferences.PREFERRED_ACTION_FAV_SHARE:
                addMenuSecondaryBrowser(ctx, builder);
                addActionBtnFavShareApp(ctx, builder);
                break;
            default:
                addActionButtonSecondaryBrowser(ctx, builder);
                addMenuFavShareApp(ctx, builder);
                break;
        }

        addCopyLink(ctx, builder);

        addShortcutToHomeScreen(ctx, builder);

        addOpenInMainBrowser(ctx, builder);

        prepareBottomBar(ctx, builder);

        return builder.build();
    }

    private static void addOpenInMainBrowser(@NonNull Context ctx, @NonNull CustomTabsIntent.Builder builder) {
        final String customTabPkg = Preferences.customTabApp(ctx);

        if (Util.isPackageInstalled(ctx, customTabPkg)) {
            if (customTabPkg.equalsIgnoreCase(CustomTabHelper.BETA_PACKAGE)
                    || customTabPkg.equalsIgnoreCase(CustomTabHelper.DEV_PACKAGE)
                    || customTabPkg.equalsIgnoreCase(CustomTabHelper.STABLE_PACKAGE)) {

                final Intent chromeReceiver = new Intent(ctx, OpenInChromeReceiver.class);
                final PendingIntent openChromePending = PendingIntent.getBroadcast(ctx, 0, chromeReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

                final String app = Util.getAppNameWithPackage(ctx, customTabPkg);
                final String label = String.format(ctx.getString(R.string.open_in_browser), app);
                builder.addMenuItem(label, openChromePending);
            }
        }
    }

    private static void addAnimations(@NonNull Context ctx, @NonNull CustomTabsIntent.Builder builder) {
        if (Preferences.isAnimationEnabled(ctx)) {
            switch (Preferences.animationSpeed(ctx)) {
                case Preferences.ANIMATION_MEDIUM:
                    switch (Preferences.animationType(ctx)) {
                        case 1:
                            builder.setStartAnimations(ctx, R.anim.slide_in_right_medium, R.anim.slide_out_left_medium)
                                    .setExitAnimations(ctx, R.anim.slide_in_left_medium, R.anim.slide_out_right_medium);
                            break;
                        case 2:
                            builder.setStartAnimations(ctx, R.anim.slide_up_right_medium, R.anim.slide_down_left_medium)
                                    .setExitAnimations(ctx, R.anim.slide_up_left_medium, R.anim.slide_down_right_medium);
                            break;
                        default:
                            builder.setStartAnimations(ctx, R.anim.slide_in_right_medium, R.anim.slide_out_left_medium)
                                    .setExitAnimations(ctx, R.anim.slide_in_left_medium, R.anim.slide_out_right_medium);
                    }
                    break;
                case Preferences.ANIMATION_SHORT:
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
                    break;
            }
        }
    }

    private static void setToolbarColor(@NonNull Context ctx, @NonNull String url, @NonNull CustomTabsIntent.Builder builder) {
        sToolbarColor = Color.WHITE;
        if (Preferences.isColoredToolbar(ctx)) {
            // Get the user chosen color first
            int toolbarColor = Preferences.toolbarColor(ctx);

            if (Preferences.dynamicToolbar(ctx)) {
                // Attempt to get the color of the calling app then
                if (Preferences.dynamicToolbarOnApp(ctx)) {
                    try {
                        final String lastApp = AppDetectService.getInstance().getLastApp();
                        final List<AppColor> appColors = AppColor.find(AppColor.class, "app = ?", lastApp);

                        if (!appColors.isEmpty()) {
                            // Extracted colors exists
                            Timber.d("Using color for %s", lastApp);
                            toolbarColor = appColors.get(0).getColor();
                        } else {
                            // Color does not exist for this app, so let's extract it
                            doExtractionForApp(ctx, lastApp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (AppDetectService.getInstance() == null) {
                            ctx.startService(new Intent(ctx, AppDetectService.class));
                        }
                    }
                }

                // Further try to get extract theme color of website
                if (Preferences.dynamicToolbarOnWeb(ctx)) {
                    // Check if we have the color extracted for this source
                    final String host = Uri.parse(url).getHost();
                    if (host != null) {
                        final List<WebColor> webColors = WebColor.find(WebColor.class, "url = ?", host);

                        if (!webColors.isEmpty()) {
                            // Extracted colors exists
                            toolbarColor = webColors.get(0).getColor();
                        } else {
                            // Color does not exist for this site, so let's extract it
                            doExtractionForUrl(ctx, url);
                        }
                    }
                }
            }
            sToolbarColor = toolbarColor;
            builder.setToolbarColor(toolbarColor);
        }
    }

    private static void doExtractionForUrl(@NonNull Context ctx, @NonNull String url) {
        final Intent extractorService = new Intent(ctx, WebColorExtractorService.class);
        extractorService.setData(Uri.parse(url));
        ctx.startService(extractorService);
    }

    private static void doExtractionForApp(@NonNull Context ctx, @NonNull String app) {
        final Intent extractorService = new Intent(ctx, AppColorExtractorService.class);
        extractorService.putExtra("app", app);
        ctx.startService(extractorService);
    }

    private static void addActionButtonSecondaryBrowser(@NonNull Context ctx, @NonNull CustomTabsIntent.Builder builder) {
        final Intent secondaryBrowserReceiver = new Intent(ctx, SecondaryBrowserReceiver.class);

        final PendingIntent openBrowserPending = PendingIntent.getBroadcast(ctx, 0, secondaryBrowserReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        final String pkg = Preferences.secondaryBrowserPackage(ctx);
        if (pkg != null && Util.isPackageInstalled(ctx, pkg)) {
            final Bitmap icon;
            try {
                icon = Util.drawableToBitmap(ctx.getApplicationContext().getPackageManager().getApplicationIcon(pkg));
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }
            builder.setActionButton(icon, "Secondary browser", openBrowserPending);
        }
    }

    private static void addMenuSecondaryBrowser(@NonNull Context ctx, @NonNull CustomTabsIntent.Builder builder) {
        try {
            final Intent secondaryBrowserReceiver = new Intent(ctx, SecondaryBrowserReceiver.class);

            final PendingIntent openBrowserPending = PendingIntent.getBroadcast(ctx, 0, secondaryBrowserReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

            final String pkg = Preferences.secondaryBrowserPackage(ctx);

            if (pkg != null && Util.isPackageInstalled(ctx, pkg)) {
                final String app = Util.getAppNameWithPackage(ctx, pkg);
                final String label = String.format(ctx.getString(R.string.open_in_browser), app);
                builder.addMenuItem(label, openBrowserPending);
            }
        } catch (Exception e) {
            Timber.d("Was not able to set secondary browser");
        }
    }

    private static void addMenuFavShareApp(@NonNull Context ctx, @NonNull CustomTabsIntent.Builder builder) {
        final Intent shareIntent = new Intent(ctx, FavShareBroadcastReceiver.class);
        final PendingIntent pendingShareIntent = PendingIntent.getBroadcast(ctx, 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final String pkg = Preferences.favSharePackage(ctx);
        if (pkg != null && Util.isPackageInstalled(ctx, pkg)) {
            final String app = Util.getAppNameWithPackage(ctx, pkg);
            final String label = String.format(ctx.getString(R.string.share_with), app);
            builder.addMenuItem(label, pendingShareIntent);
        }
    }

    private static void addActionBtnFavShareApp(@NonNull Context ctx, @NonNull CustomTabsIntent.Builder builder) {
        final Intent shareIntent = new Intent(ctx, FavShareBroadcastReceiver.class);
        final PendingIntent pendingShareIntent = PendingIntent.getBroadcast(ctx, 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final String pkg = Preferences.favSharePackage(ctx);
        if (pkg != null && Util.isPackageInstalled(ctx, pkg)) {
            final Bitmap icon;
            try {
                icon = Util.drawableToBitmap(ctx.getApplicationContext().getPackageManager().getApplicationIcon(pkg));
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }
            builder.setActionButton(icon, "Fav share app", pendingShareIntent);
        }
    }

    @Nullable
    private static CustomTabsSession getAvailableSessions(@NonNull Context ctx, boolean isWebhead) {
        if (isWebhead && WebHeadService.getInstance() != null) {
            Timber.d("Using webhead session");
            return WebHeadService.getInstance().getTabSession();
        } else {
            if (Preferences.warmUp(ctx)) {
                ctx.startService(new Intent(ctx, WarmupService.class));
            }
        }

        ScannerService sService = ScannerService.getInstance();
        if (sService != null && Preferences.preFetch(ctx)) {
            Timber.d("Using scanner session");
            return sService.getTabSession();
        }
        WarmupService service = WarmupService.getInstance();
        if (service != null) {
            Timber.d("Using warmup session");
            return service.getTabSession();
        }
        Timber.d("No existing sessions present");
        return null;
    }

    private static void addShortcutToHomeScreen(@NonNull Context c, @NonNull CustomTabsIntent.Builder builder) {
        final Intent addShortcutIntent = new Intent(c, AddHomeShortcutService.class);
        final PendingIntent addShortcutPending = PendingIntent.getService(c, 0, addShortcutIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addMenuItem(c.getString(R.string.add_to_homescreen), addShortcutPending);
    }

    private static void addCopyLink(@NonNull Context c, @NonNull CustomTabsIntent.Builder builder) {
        final Intent clipboardIntent = new Intent(c, ClipboardService.class);
        final PendingIntent serviceIntentPending = PendingIntent.getService(c, 0, clipboardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addMenuItem(c.getString(R.string.copy_link), serviceIntentPending);
    }

    private static void addShareIntent(@NonNull Context c, @NonNull CustomTabsIntent.Builder builder) {
        if (Preferences.bottomBar(c)) {
            return;
        }

        final Intent shareIntent = new Intent(c, ShareBroadcastReceiver.class);
        final PendingIntent pendingShareIntent = PendingIntent.getBroadcast(c, 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addMenuItem(c.getString(R.string.share), pendingShareIntent);
    }

    private static void prepareBottomBar(@NonNull Context c, @NonNull CustomTabsIntent.Builder builder) {
        if (!Preferences.bottomBar(c)) {
            return;
        }

        builder.setSecondaryToolbarColor(sToolbarColor);

        int iconColor = Util.getForegroundTextColor(sToolbarColor);

        if (Util.isLollipop()) {
            final Intent openInNewTabIntent = new Intent(c, OpenInNewTabReceiver.class);
            final PendingIntent pendingOpenTabIntent = PendingIntent.getBroadcast(c, 0, openInNewTabIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap openTabIcon = new IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon.cmd_plus_box)
                    .color(iconColor)
                    .sizeDp(24).toBitmap();
            builder.addToolbarItem(OPEN_TAB, openTabIcon, c.getString(R.string.open_in_new_tab), pendingOpenTabIntent);
        }

        final Intent shareIntent = new Intent(c, ShareBroadcastReceiver.class);
        final PendingIntent pendingShareIntent = PendingIntent.getBroadcast(c, 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap shareIcon = new IconicsDrawable(c)
                .icon(GoogleMaterial.Icon.gmd_share)
                .color(iconColor)
                .sizeDp(24).toBitmap();
        builder.addToolbarItem(SHARE_TAB, shareIcon, c.getString(R.string.share), pendingShareIntent);
    }

}
