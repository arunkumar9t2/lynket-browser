package arun.com.chromer.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.ViewOutlineProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.customtabs.CustomTabHelper;
import arun.com.chromer.customtabs.prefetch.ScannerService;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.views.IntentPickerSheetView;

/**
 * Created by Arun on 17/12/2015.
 */
@SuppressWarnings("JavaDoc")
public class Util {

    public static boolean isLollipopAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void openPlayStore(@NonNull Context context, @NonNull String appPackageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @Nullable
    public static List<String> findURLs(@Nullable String string) {
        if (string == null) {
            return null;
        }
        List<String> links = new ArrayList<>();
        Matcher m = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(string);
        while (m.find()) {
            String url = m.group();
            if (!url.toLowerCase().matches("^\\w+://.*")) {
                url = "http://" + url;
            }
            links.add(url);
        }

        return links;
    }

    public static boolean isPackageInstalled(@NonNull Context c, @Nullable String pkgName) {
        if (pkgName == null) return false;

        PackageManager pm = c.getApplicationContext().getPackageManager();
        try {
            pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @NonNull
    public static String getAppNameWithPackage(@NonNull Context context, @NonNull String pack) {
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(pack, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    /**
     * Iterates through available browsers packages and returns the ComponentName for the given
     * package.
     *
     * @param context Context to retrieve package list
     * @param pkg     The package name
     * @return
     */
    @Nullable
    public static ComponentName getBrowserComponentForPackage(@NonNull Context context, @NonNull String pkg) {
        Intent webIntentImplicit = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_URL));
        @SuppressLint("InlinedApi") List<ResolveInfo> resolvedActivityList = context.getApplicationContext().getPackageManager()
                .queryIntentActivities(webIntentImplicit, PackageManager.MATCH_ALL);

        ComponentName componentName = null;
        for (ResolveInfo info : resolvedActivityList) {
            if (info.activityInfo.packageName.equalsIgnoreCase(pkg)) {
                componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                webIntentImplicit.setComponent(componentName);
            }
        }
        return componentName;
    }

    public static boolean isAccessibilityServiceEnabled(@NonNull Context context) {
        int accesEnbld = 0;
        final String service = BuildConfig.APPLICATION_ID + "/" + ScannerService.class.getName();
        try {
            accesEnbld = Settings.Secure.getInt(context.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');

        if (accesEnbld == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String aService = splitter.next();
                    if (aService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @NonNull
    public static String getDefaultBrowserPackage(@NonNull Context context) {
        ResolveInfo resolveInfo = context.getApplicationContext()
                .getPackageManager()
                .resolveActivity(Constants.WEB_INTENT,
                        PackageManager.MATCH_DEFAULT_ONLY);

        return resolveInfo != null ? resolveInfo.activityInfo.packageName.trim() : "";
    }

    public static boolean isDefaultBrowser(@NonNull Context context) {
        return getDefaultBrowserPackage(context).equalsIgnoreCase(context.getPackageName());
    }

    @NonNull
    public static List<IntentPickerSheetView.ActivityInfo> getCustomTabApps(@NonNull Context context) {
        List<IntentPickerSheetView.ActivityInfo> apps = new ArrayList<>();
        PackageManager pm = context.getApplicationContext().getPackageManager();
        @SuppressLint("InlinedApi") List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(Constants.WEB_INTENT, PackageManager.MATCH_ALL);
        for (ResolveInfo info : resolvedActivityList) {
            final String packageName = info.activityInfo.packageName;
            if (CustomTabHelper.isPackageSupportCustomTabs(context, packageName)) {
                ComponentName componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                IntentPickerSheetView.ActivityInfo activityInfo = new IntentPickerSheetView.ActivityInfo(info, info.loadLabel(pm), componentName);
                apps.add(activityInfo);
            }
        }
        return apps;
    }

    @NonNull
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap;

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


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean canReadUsageStats(@NonNull Context context) {
        // http://stackoverflow.com/questions/27215013/check-if-my-application-has-usage-access-enabled
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Nullable
    public static String getSearchUrl(@Nullable String text) {
        if (text == null) return null;
        if (Patterns.WEB_URL.matcher(text).matches()) {
            if (!text.toLowerCase().matches("^\\w+://.*")) {
                text = "http://" + text;
            }
            return text;
        } else
            return Constants.G_SEARCH_URL + text.replace(" ", "+");
    }

    public static boolean isVoiceRecognizerPresent(@NonNull Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return (activities != null) && (activities.size() > 0);
    }

    public static int dpToPx(double dp) {
        // resources instead of context !!
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    @SuppressWarnings("unused")
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * A helper class for providing a shadow on sheets
     */
    @TargetApi(21)
    public static class ShadowOutline extends ViewOutlineProvider {

        final int width;
        final int height;

        public ShadowOutline(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, width, height);
        }
    }

}
