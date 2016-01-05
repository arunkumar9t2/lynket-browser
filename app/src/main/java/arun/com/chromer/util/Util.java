package arun.com.chromer.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.R;
import arun.com.chromer.chrometabutilites.AddHomeShortcutReceiver;
import arun.com.chromer.chrometabutilites.ShareBroadcastReceiver;
import arun.com.chromer.services.ClipboardService;

/**
 * Created by Arun on 17/12/2015.
 */
public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static CustomTabsIntent getCustomizedTabIntent(
            Context c,
            String url) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        if (sharedPreferences.getBoolean("toolbar_color_pref", true)) {
            final int chosenColor = sharedPreferences.getInt("toolbar_color",
                    ContextCompat.getColor(c, R.color.colorPrimary));
            builder.setToolbarColor(chosenColor);
        }

        if (sharedPreferences.getBoolean("animations_pref", true)) {
            switch (Integer.parseInt(sharedPreferences.getString("animation_preference", "1"))) {
                case 1:
                    builder.setStartAnimations(c, R.anim.slide_in_right, R.anim.slide_out_left)
                            .setExitAnimations(c, R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case 2:
                    builder.setStartAnimations(c, R.anim.slide_up_right, R.anim.slide_down_left)
                            .setExitAnimations(c, R.anim.slide_up_left, R.anim.slide_down_right);
                    break;
                default:
                    builder.setStartAnimations(c, R.anim.slide_in_right, R.anim.slide_out_left)
                            .setExitAnimations(c, R.anim.slide_in_left, R.anim.slide_out_right);
            }

        }

        if (sharedPreferences.getBoolean("url_pref", true)) {
            builder.enableUrlBarHiding();
        }

        if (sharedPreferences.getBoolean("title_pref", true)) {
            builder.setShowTitle(true);
        } else
            builder.setShowTitle(false);

        addShareIntent(c, url, builder);

        addCopyItem(c, url, builder);

        addShortcuttoHomescreen(c, url, builder);
        return builder.build();
    }

    private static void addShortcuttoHomescreen(Context c, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent addShortcutIntent = new Intent(c, AddHomeShortcutReceiver.class);
            addShortcutIntent.putExtra(Intent.EXTRA_TEXT, url);
            PendingIntent addShortcut = PendingIntent
                    .getBroadcast(c, 0, addShortcutIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addMenuItem(c.getString(R.string.add_to_homescreen), addShortcut);
        }
    }

    private static void addCopyItem(Context c, String url, CustomTabsIntent.Builder builder) {
        if (url != null) {
            Intent clipboardIntent = new Intent(c, ClipboardService.class);
            clipboardIntent.putExtra(Intent.EXTRA_TEXT, url);
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

    public static void openPlayStore(Context context, String appPackageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }


    public static List<String> findURLs(String string) {
        if (string == null) {
            return null;
        }
        List<String> links = new ArrayList<>();
        Matcher m = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(string);
        while (m.find()) {
            String url = m.group();
            Log.d(TAG, "URL extracted: " + url);
            if (!url.toLowerCase().matches("^\\w+://.*")) {
                url = "http://" + url;
            }
            links.add(url);
        }

        return links;
    }

    public static String getPackageVersion(Context context) {
        String versionName;
        try {
            versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
        return versionName;
    }

    public static boolean isPackageInstalled(Context c, String packagename) {
        PackageManager pm = c.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String[] getAppNameFromPackages(Context context, List<String> packages) {
        List<String> appNameList = new ArrayList<>();
        String[] appNames = new String[0];
        for (String pack : packages) {
            final PackageManager pm = context.getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(pack, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
            appNameList.add(applicationName);
        }
        appNames = appNameList.toArray(appNames);
        return appNames;
    }


}
