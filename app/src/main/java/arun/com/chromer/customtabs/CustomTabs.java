package arun.com.chromer.customtabs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import arun.com.chromer.R;
import arun.com.chromer.customtabs.bottombar.BottomBarManager;
import arun.com.chromer.customtabs.callbacks.AddHomeShortcutService;
import arun.com.chromer.customtabs.callbacks.ClipboardService;
import arun.com.chromer.customtabs.callbacks.FavShareBroadcastReceiver;
import arun.com.chromer.customtabs.callbacks.MinimizeBroadcastReceiver;
import arun.com.chromer.customtabs.callbacks.OpenInChromeReceiver;
import arun.com.chromer.customtabs.callbacks.SecondaryBrowserReceiver;
import arun.com.chromer.customtabs.callbacks.ShareBroadcastReceiver;
import arun.com.chromer.customtabs.dynamictoolbar.AppColorExtractorService;
import arun.com.chromer.customtabs.dynamictoolbar.WebColorExtractorService;
import arun.com.chromer.customtabs.prefetch.ScannerService;
import arun.com.chromer.customtabs.warmup.WarmUpService;
import arun.com.chromer.db.AppColor;
import arun.com.chromer.db.WebColor;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.shared.AppDetectionManager;
import arun.com.chromer.util.Utils;
import arun.com.chromer.webheads.WebHeadService;
import timber.log.Timber;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.graphics.Color.WHITE;
import static arun.com.chromer.customtabs.bottombar.BottomBarManager.createBottomBarRemoteViews;
import static arun.com.chromer.customtabs.bottombar.BottomBarManager.getClickableIDs;
import static arun.com.chromer.customtabs.bottombar.BottomBarManager.getOnClickPendingIntent;
import static arun.com.chromer.preferences.manager.Preferences.ANIMATION_MEDIUM;
import static arun.com.chromer.preferences.manager.Preferences.ANIMATION_SHORT;
import static arun.com.chromer.preferences.manager.Preferences.PREFERRED_ACTION_BROWSER;
import static arun.com.chromer.preferences.manager.Preferences.PREFERRED_ACTION_FAV_SHARE;
import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * A helper class that builds up the view intent according to user preferences and
 * launches custom tab.
 */
public class CustomTabs {
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";
    private static final String EXTRA_CUSTOM_TABS_KEEP_ALIVE = "android.support.customtabs.extra.KEEP_ALIVE";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";

    /**
     * Fallback in case there was en error launching custom tabs
     */
    private final static CustomTabsFallback CUSTOM_TABS_FALLBACK =
            new CustomTabsFallback() {
                @Override
                public void openUri(Activity activity, Uri uri) {
                    if (activity != null) {
                        final String string = activity.getString(R.string.fallback_msg);
                        Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
                        try {
                            final Intent target = new Intent(Intent.ACTION_VIEW, uri);
                            activity.startActivity(Intent.createChooser(target, activity.getString(R.string.open_with)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity, activity.getString(R.string.unxp_err), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };
    /**
     * The context to work with
     */
    private Activity activity;
    /**
     * The url for which the custom tab should be launched;
     */
    private String url;
    /**
     * The builder used to customize the custom tab intent
     */
    private CustomTabsIntent.Builder builder;
    /**
     * Client provided custom tab session
     */
    private CustomTabsSession customTabsSession;
    @ColorInt
    private int toolbarColor = NO_COLOR;
    /**
     * Toolbar color that overrides the default toolbar color generated by this helper.
     */
    @ColorInt
    private int webToolbarFallback = NO_COLOR;
    /**
     * True if this tab intent is used for web heads. False otherwise.
     */
    private boolean forWebHead = false;
    // Whether to use animations
    private boolean noAnimation = false;

    /**
     * Private constructor to init our helper
     *
     * @param context the context to work with
     */

    private CustomTabs(Activity context) {
        activity = context;
        forWebHead = false;
        noAnimation = false;
    }

    /**
     * Used to get an instance of this helper
     *
     * @param activity The context from which custom tab should be launched
     * @return A helper instance
     */
    public static CustomTabs from(@NonNull Activity activity) {
        return new CustomTabs(activity);
    }

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     */
    private void openCustomTab() {
        final String packageName = getCustomTabPackage(activity);
        final CustomTabsIntent customTabsIntent = builder.build();
        final Uri uri = Uri.parse(url);
        if (packageName != null) {
            customTabsIntent.intent.setPackage(packageName);
            final Intent keepAliveIntent = new Intent();
            keepAliveIntent.setClassName(activity.getPackageName(), KeepAliveService.class.getCanonicalName());
            customTabsIntent.intent.putExtra(EXTRA_CUSTOM_TABS_KEEP_ALIVE, keepAliveIntent);
            if (noAnimation) {
                // customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            try {
                customTabsIntent.launchUrl(activity, uri);
                Timber.d("Launched url: %s", uri.toString());
            } catch (Exception e) {
                CUSTOM_TABS_FALLBACK.openUri(activity, uri);
                Timber.e("Called fallback even though a package was found, weird Exception : %s", e.toString());
            }
        } else {
            Timber.e("Called fallback since no package found!");
            CUSTOM_TABS_FALLBACK.openUri(activity, uri);
        }
    }

    /**
     * Attempts to find the custom the best custom tab package to use.
     *
     * @return A package that supports custom tab, null if not present
     */
    @Nullable
    private static String getCustomTabPackage(Context context) {
        final String userPackage = Preferences.customTabApp(context);
        if (userPackage != null && userPackage.length() > 0) {
            return userPackage;
        }
        if (isPackageSupportCustomTabs(context, STABLE_PACKAGE))
            return STABLE_PACKAGE;
        if (isPackageSupportCustomTabs(context, LOCAL_PACKAGE))
            return LOCAL_PACKAGE;

        final List<String> supportingPackages = getCustomTabSupportingPackages(context);
        if (!supportingPackages.isEmpty()) {
            return supportingPackages.get(0);
        } else
            return null;
    }

    /**
     * Returns all valid custom tab supporting browser packages on the system. Does not respect if
     * the package is default or not.
     *
     * @param context context to work with
     * @return list of packages supporting CCT
     */
    @TargetApi(Build.VERSION_CODES.M)
    @NonNull
    public static List<String> getCustomTabSupportingPackages(Context context) {
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        final Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        // Get all apps that can handle VIEW intents.
        final List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
        final List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            if (isPackageSupportCustomTabs(context, info.activityInfo.packageName)) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }
        return packagesSupportingCustomTabs;
    }

    /**
     * Determines if the provided package name is a valid custom tab provider or not.
     *
     * @param context     Context to work with
     * @param packageName Package name of the app
     * @return true if a provider, false otherwise
     */
    public static boolean isPackageSupportCustomTabs(Context context, @Nullable String packageName) {
        if (packageName == null) {
            return false;
        }
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        final Intent serviceIntent = new Intent();
        serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
        serviceIntent.setPackage(packageName);
        return pm.resolveService(serviceIntent, 0) != null;
    }

    public CustomTabs withSession(@Nullable CustomTabsSession session) {
        if (session != null) {
            customTabsSession = session;
        }
        return this;
    }

    /**
     * Exposed method to set the url for which this CCT should be launched
     *
     * @param url Url of the web site
     */
    public CustomTabs forUrl(@NonNull final String url) {
        this.url = url.trim();
        return this;
    }

    /**
     * To set if the custom tab is going to be launched from web heads. Default is false.
     *
     * @param isWebHead true if for web heads
     * @return Instance of this class
     */
    public CustomTabs forWebHead(boolean isWebHead) {
        forWebHead = isWebHead;
        return this;
    }

    /**
     * Clients can specify a toolbar color that will override whatever {@link #prepareToolbar()} sets.
     * Alpha value of the provided color will be ignored.
     * <p>
     * Note: {@link #prepareToolbar()} can choose to ignore this value based on user preference like
     * dynamic coloring. Refer that method for details.
     *
     * @param overrideColor color to override
     */
    public CustomTabs fallbackColor(@ColorInt int overrideColor) {
        webToolbarFallback = ColorUtils.setAlphaComponent(overrideColor, 0xFF);
        return this;
    }

    /**
     * Facade method that does all the heavy work of building up the builder based on user preferences
     *
     * @return Instance of this class
     */
    @NonNull
    public CustomTabs prepare() {
        builder = new CustomTabsIntent.Builder(getSession());
        // set defaults
        builder.setShowTitle(true);
        builder.enableUrlBarHiding();
        builder.addDefaultShareMenuItem();

        prepareAnimations();
        prepareToolbar();
        prepareActionButton();
        prepareMenuItems();
        prepareBottomBar();
        return this;
    }

    /**
     * Builds custom tab intent from the builder we created so far and launches the custom tab.
     */
    public void launch() {
        assertBuilderInitialized();
        openCustomTab();

        // Dispose reference
        activity = null;
        customTabsSession = null;
    }

    /**
     * Tries to find available sessions for the url to launch in.
     *
     * @return Instance of this class
     */
    @Nullable
    private CustomTabsSession getSession() {
        if (customTabsSession != null) {
            return customTabsSession;
        }
        if (WebHeadService.getTabSession() != null) {
            Timber.d("Using webhead session");
            return WebHeadService.getTabSession();
        }

        final ScannerService sService = ScannerService.getInstance();
        if (sService != null && sService.getTabSession() != null && Preferences.preFetch(activity)) {
            Timber.d("Using scanner session");
            return sService.getTabSession();
        }
        final WarmUpService service = WarmUpService.getInstance();
        if (service != null) {
            Timber.d("Using warm up session");
            return service.getTabSession();
        }
        return null;
    }

    /**
     * Used to set the correct custom tab opening/closing animations. Will re use last used animations
     * if the preference did not change from before.
     */
    private void prepareAnimations() {
        assertBuilderInitialized();
        if (Preferences.isAnimationEnabled(activity) && !noAnimation) {
            final int type = Preferences.animationType(activity);
            final int speed = Preferences.animationSpeed(activity);
            int start[] = new int[]{};
            int exit[] = new int[]{};
            switch (speed) {
                case ANIMATION_MEDIUM:
                    switch (type) {
                        case 1:
                            start = new int[]{R.anim.slide_in_right_medium, R.anim.slide_out_left_medium};
                            exit = new int[]{R.anim.slide_in_left_medium, R.anim.slide_out_right_medium};
                            break;
                        case 2:
                            start = new int[]{R.anim.slide_up_right_medium, R.anim.slide_down_left_medium};
                            exit = new int[]{R.anim.slide_up_left_medium, R.anim.slide_down_right_medium};
                            break;
                    }
                    break;
                case ANIMATION_SHORT:
                    switch (type) {
                        case 1:
                            start = new int[]{R.anim.slide_in_right, R.anim.slide_out_left};
                            exit = new int[]{R.anim.slide_in_left, R.anim.slide_out_right};
                            break;
                        case 2:
                            start = new int[]{R.anim.slide_up_right, R.anim.slide_down_left};
                            exit = new int[]{R.anim.slide_up_left, R.anim.slide_down_right};
                            break;
                    }
                    break;
            }
            // set it to builder
            builder
                    .setStartAnimations(activity, start[0], start[1])
                    .setExitAnimations(activity, exit[0], exit[1]);
            activity.overridePendingTransition(start[0], start[1]);
        }
    }

    /**
     * Method to handle tool bar color. Takes care of handling secondary toolbar color as well.
     */
    private void prepareToolbar() {
        assertBuilderInitialized();
        if (Preferences.isColoredToolbar(activity)) {
            toolbarColor = Preferences.toolbarColor(activity);

            if (Preferences.dynamicToolbar(activity)) {
                final boolean overrideRequested = webToolbarFallback != NO_COLOR;
                if (Preferences.dynamicToolbarOnApp(activity)) {
                    setAppToolbarColor();
                }

                if (Preferences.dynamicToolbarOnWeb(activity)) {
                    if (overrideRequested) {
                        toolbarColor = webToolbarFallback;
                        Timber.d("Using fallback color");
                    } else {
                        setWebToolbarColor();
                    }
                }
            }

            if (toolbarColor != NO_COLOR) {
                builder
                        .setToolbarColor(toolbarColor)
                        .setSecondaryToolbarColor(toolbarColor);
            }
        }
    }

    /**
     * Sets the toolbar color based on the web site we are launching for
     *
     * @return Whether color setting was successful or not.
     */
    private boolean setWebToolbarColor() {
        // Check if we have the color extracted for this source
        final String host = Uri.parse(url).getHost();
        if (host != null) {
            final List<WebColor> webColors = WebColor.find(WebColor.class, "url = ?", host);

            if (!webColors.isEmpty()) {
                toolbarColor = webColors.get(0).getColor();
                return true;
            } else {
                final Intent extractorService = new Intent(activity, WebColorExtractorService.class);
                extractorService.setData(Uri.parse(url));
                activity.startService(extractorService);
            }
        }
        return false;
    }

    /**
     * Sets the toolbar color based on launching app.
     *
     * @return Whether color setting was successful or not.
     */
    private boolean setAppToolbarColor() {
        try {
            final String lastApp = AppDetectionManager.getInstance(activity).getFilteredPackage();
            if (TextUtils.isEmpty(lastApp)) return false;
            final List<AppColor> appColors = AppColor.find(AppColor.class, "app = ?", lastApp);
            if (!appColors.isEmpty()) {
                toolbarColor = appColors.get(0).getColor();
                return true;
            } else {
                final Intent extractorService = new Intent(activity, AppColorExtractorService.class);
                extractorService.putExtra("app", lastApp);
                activity.startService(extractorService);
            }
        } catch (Exception e) {
            Timber.e(e.toString());
        }
        return false;
    }

    /**
     * Used to set the action button based on user preferences. Usually secondary browser or favorite share app.
     */
    private void prepareActionButton() {
        assertBuilderInitialized();
        switch (Preferences.preferredAction(activity)) {
            case PREFERRED_ACTION_BROWSER:
                String pakage = Preferences.secondaryBrowserPackage(activity);
                if (Utils.isPackageInstalled(activity, pakage)) {
                    final Bitmap icon = getAppIconBitmap(pakage);
                    final Intent intent = new Intent(activity, SecondaryBrowserReceiver.class);
                    final PendingIntent openBrowserPending = PendingIntent.getBroadcast(activity, 0, intent, FLAG_UPDATE_CURRENT);
                    //noinspection ConstantConditions
                    builder.setActionButton(icon, activity.getString(R.string.choose_secondary_browser), openBrowserPending);
                }
                break;
            case PREFERRED_ACTION_FAV_SHARE:
                pakage = Preferences.favSharePackage(activity);
                if (Utils.isPackageInstalled(activity, pakage)) {
                    final Bitmap icon = getAppIconBitmap(pakage);
                    final Intent intent = new Intent(activity, FavShareBroadcastReceiver.class);
                    final PendingIntent favSharePending = PendingIntent.getBroadcast(activity, 0, intent, FLAG_UPDATE_CURRENT);
                    //noinspection ConstantConditions
                    builder.setActionButton(icon, activity.getString(R.string.fav_share_app), favSharePending);
                }
                break;
            case Preferences.PREFERRED_ACTION_GEN_SHARE:
                final Bitmap shareIcon = new IconicsDrawable(activity)
                        .icon(CommunityMaterial.Icon.cmd_share_variant)
                        .color(WHITE)
                        .sizeDp(24).toBitmap();
                final Intent intent = new Intent(activity, ShareBroadcastReceiver.class);
                final PendingIntent sharePending = PendingIntent.getBroadcast(activity, 0, intent, FLAG_UPDATE_CURRENT);
                //noinspection ConstantConditions
                builder.setActionButton(shareIcon, activity.getString(R.string.share_via), sharePending, true);
                break;
        }
    }

    /**
     * Prepares all the menu items and adds to builder
     */
    private void prepareMenuItems() {
        assertBuilderInitialized();
        preparePreferredAction();
        prepareMinimize();
        prepareCopyLink();
        prepareAddToHomeScreen();
        prepareOpenInChrome();
    }

    /**
     * Adds a menu item tapping which will minimize the current custom tab back to overview. This requires
     * merge tabs and apps and
     */
    private void prepareMinimize() {
        if (!Preferences.bottomBar(activity) && forWebHead && Preferences.mergeTabs(activity)) {
            final Intent minimizeIntent = new Intent(activity, MinimizeBroadcastReceiver.class);
            minimizeIntent.putExtra(Intent.EXTRA_TEXT, url);
            final PendingIntent pendingMin = PendingIntent.getBroadcast(activity, new Random().nextInt(), minimizeIntent, FLAG_UPDATE_CURRENT);
            builder.addMenuItem(activity.getString(R.string.minimize), pendingMin);
        }
    }

    /**
     * Opposite of what {@link #prepareActionButton()} does. Fills a menu item with either secondary
     * browser or favorite share app.
     */
    private void preparePreferredAction() {
        assertBuilderInitialized();
        switch (Preferences.preferredAction(activity)) {
            case PREFERRED_ACTION_BROWSER:
                String pkg = Preferences.favSharePackage(activity);
                if (Utils.isPackageInstalled(activity, pkg)) {
                    final String app = Utils.getAppNameWithPackage(activity, pkg);
                    final String label = String.format(activity.getString(R.string.share_with), app);
                    final Intent shareIntent = new Intent(activity, FavShareBroadcastReceiver.class);
                    final PendingIntent pendingShareIntent = PendingIntent.getBroadcast(activity, 0, shareIntent, FLAG_UPDATE_CURRENT);
                    builder.addMenuItem(label, pendingShareIntent);
                }
                break;
            case PREFERRED_ACTION_FAV_SHARE:
                pkg = Preferences.secondaryBrowserPackage(activity);
                if (Utils.isPackageInstalled(activity, pkg)) {
                    if (!pkg.equalsIgnoreCase(STABLE_PACKAGE)) {
                        final String app = Utils.getAppNameWithPackage(activity, pkg);
                        final String label = String.format(activity.getString(R.string.open_in_browser), app);
                        final Intent browseIntent = new Intent(activity, SecondaryBrowserReceiver.class);
                        final PendingIntent pendingBrowseIntent = PendingIntent.getBroadcast(activity, 0, browseIntent, FLAG_UPDATE_CURRENT);
                        builder.addMenuItem(label, pendingBrowseIntent);
                    } else {
                        Timber.d("Excluded secondary browser menu as it was Chrome");
                    }
                }
                break;
        }
    }

    private void prepareCopyLink() {
        final Intent clipboardIntent = new Intent(activity, ClipboardService.class);
        final PendingIntent serviceIntentPending = PendingIntent.getService(activity, 0, clipboardIntent, FLAG_UPDATE_CURRENT);
        builder.addMenuItem(activity.getString(R.string.copy_link), serviceIntentPending);
    }

    /**
     * Adds menu item to enable adding the current url to home screen
     */
    private void prepareAddToHomeScreen() {
        final Intent addShortcutIntent = new Intent(activity, AddHomeShortcutService.class);
        final PendingIntent addShortcutPending = PendingIntent.getService(activity, 0, addShortcutIntent, FLAG_UPDATE_CURRENT);
        builder.addMenuItem(activity.getString(R.string.add_to_homescreen), addShortcutPending);
    }

    /**
     * Adds an open in chrome option
     */
    private void prepareOpenInChrome() {
        final String customTabPkg = Preferences.customTabApp(activity);

        if (Utils.isPackageInstalled(activity, customTabPkg)) {
            if (customTabPkg.equalsIgnoreCase(BETA_PACKAGE)
                    || customTabPkg.equalsIgnoreCase(DEV_PACKAGE)
                    || customTabPkg.equalsIgnoreCase(STABLE_PACKAGE)) {

                final Intent chromeReceiver = new Intent(activity, OpenInChromeReceiver.class);
                final PendingIntent openChromePending = PendingIntent.getBroadcast(activity, 0, chromeReceiver, FLAG_UPDATE_CURRENT);

                final String app = Utils.getAppNameWithPackage(activity, customTabPkg);
                final String label = String.format(activity.getString(R.string.open_in_browser), app);
                builder.addMenuItem(label, openChromePending);
            }
        }
    }

    /**
     * Add all bottom bar actions
     */
    private void prepareBottomBar() {
        if (!Preferences.bottomBar(activity)) {
            return;
        }
        final BottomBarManager.Config config = new BottomBarManager.Config();
        config.minimize = forWebHead && Preferences.mergeTabs(activity);
        config.openInNewTab = Utils.isLollipopAbove();

        builder.setSecondaryToolbarViews(
                createBottomBarRemoteViews(activity, toolbarColor, config),
                getClickableIDs(),
                getOnClickPendingIntent(activity, url)
        );
    }

    /**
     * Method to check if the builder was initialized. Will fail fast if not.
     */
    private void assertBuilderInitialized() {
        if (builder == null) {
            throw new IllegalStateException("Intent builder null. Are you sure you called prepare()");
        }
    }

    /**
     * Returns the bitmap of the app icon. It is assumed, the package is installed.
     *
     * @return App icon bitmap
     */
    @Nullable
    private Bitmap getAppIconBitmap(@NonNull final String packageName) {
        try {
            final Drawable drawable = activity.getPackageManager().getApplicationIcon(packageName);
            final Bitmap appIcon = Utils.drawableToBitmap(drawable);
            return Utils.scale(appIcon, Utils.dpToPx(24), true);
        } catch (Exception e) {
            Timber.e("App icon fetching for %s failed", packageName);
        }
        return null;
    }

    public CustomTabs noAnimations(boolean noAnimations) {
        noAnimation = noAnimations;
        return this;
    }


    /**
     * To be used as a fallback to open the Uri when Custom Tabs is not available.
     */
    interface CustomTabsFallback {
        /**
         * @param activity The Activity that wants to open the Uri.
         * @param uri      The uri to be opened by the fallback.
         */
        void openUri(Activity activity, Uri uri);
    }
}
