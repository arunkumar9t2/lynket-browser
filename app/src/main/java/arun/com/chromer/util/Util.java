package arun.com.chromer.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Arun on 17/12/2015.
 */
public class Util {
    private static final String TAG = Util.class.getSimpleName();

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

            appNameList.add(getAppNameWithPackage(context, pack));
        }
        appNames = appNameList.toArray(appNames);
        return appNames;
    }

    public static String getAppNameWithPackage(Context context, String pack) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(pack, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        return applicationName;
    }
}
