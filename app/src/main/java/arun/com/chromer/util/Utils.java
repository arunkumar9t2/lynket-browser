/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.R;
import arun.com.chromer.customtabs.CustomTabs;
import arun.com.chromer.customtabs.prefetch.ScannerService;
import arun.com.chromer.data.common.App;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.views.IntentPickerSheetView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Arun on 17/12/2015.
 */
public class Utils {

    private Utils() {
        throw new RuntimeException("No instances");
    }

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

    @NonNull
    public static List<String> findURLs(@Nullable String string) {
        if (string == null) {
            return new ArrayList<>();
        }
        final List<String> links = new ArrayList<>();
        final Matcher m = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
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
    public static List<IntentPickerSheetView.ActivityInfo> getCustomTabActivityInfos(@NonNull Context context) {
        List<IntentPickerSheetView.ActivityInfo> apps = new ArrayList<>();
        PackageManager pm = context.getApplicationContext().getPackageManager();
        @SuppressLint("InlinedApi")
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(Constants.WEB_INTENT, PackageManager.MATCH_ALL);
        for (ResolveInfo info : resolvedActivityList) {
            final String packageName = info.activityInfo.packageName;
            if (CustomTabs.isPackageSupportCustomTabs(context, packageName)) {
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
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean canReadUsageStats(@NonNull Context context) {
        // http://stackoverflow.com/questions/27215013/check-if-my-application-has-usage-access-enabled
        if (!isLollipopAbove()) return true;
        try {
            final PackageManager packageManager = context.getApplicationContext().getPackageManager();
            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            final AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @NonNull
    public static String getSearchUrl(@Nullable String text) {
        if (text == null) text = "";
        if (Patterns.WEB_URL.matcher(text).matches()) {
            if (!text.toLowerCase().matches("^\\w+://.*")) {
                text = "http://" + text;
            }
            return text;
        } else
            return Constants.G_SEARCH_URL + text.replace(" ", "+");
    }

    public static boolean isVoiceRecognizerPresent(@NonNull Context context) {
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return (activities != null) && (activities.size() > 0);
    }

    public static int dpToPx(double dp) {
        // resources instead of context !!
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    public static int sp2px(float spValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @SuppressWarnings("unused")
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    @NonNull
    public static String getFirstLetter(@Nullable String address) {
        String result = "X";
        if (address != null) {
            try {
                URL url = new URL(address);
                String host = url.getHost();
                if (host != null && host.length() != 0) {
                    if (host.startsWith("www")) {
                        String[] splits = host.split("\\.");
                        if (splits.length > 1) result = String.valueOf(splits[1].charAt(0));
                        else result = String.valueOf(splits[0].charAt(0));
                    } else
                        result = String.valueOf(host.charAt(0));
                } else {
                    if (address.length() != 0) {
                        return String.valueOf(address.charAt(0));
                    }
                }
            } catch (Exception e) {
                if (address.length() != 0) {
                    return String.valueOf(address.charAt(0));
                } else return result;
            }
        }
        return result;
    }

    @NonNull
    public static Intent getRecognizerIntent(@NonNull final Context context) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_prompt));
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        return intent;
    }

    public static boolean isNetworkAvailable(@NonNull Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Nullable
    public static String getClipBoardText(@NonNull Context context) {
        final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            if (clipboardManager.hasPrimaryClip() & clipboardManager.getPrimaryClip().getItemCount() != 0) {
                final ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                if (item != null && item.getText() != null) {
                    return item.getText().toString();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void shareText(@NonNull Context context, @Nullable String url) {
        if (url != null) {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);

            final Intent chooserIntent = Intent.createChooser(shareIntent, "Share url..");
            chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooserIntent);
        } else {
            Toast.makeText(context, R.string.invalid_link, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Scales down a source image to {@param targetSizePx} and returns a scaled bitmap
     *
     * @param orgImage     Original bitmap
     * @param targetSizePx Target size in pixels
     * @param filter       Whether to use filter or not.
     * @return
     */
    public static Bitmap scale(@NonNull Bitmap orgImage, float targetSizePx, boolean filter) {
        final float ratio = Math.min(targetSizePx / orgImage.getWidth(), targetSizePx / orgImage.getHeight());
        final int width = Math.round(ratio * orgImage.getWidth());
        final int height = Math.round(ratio * orgImage.getHeight());
        return Bitmap.createScaledBitmap(orgImage, width, height, filter);
    }

    public static void printThread() {
        Timber.d("Thread: %s", Thread.currentThread().getName());
    }

    public static App createApp(@NonNull Context context, @NonNull String packageName) {
        final App app = new App();
        app.packageName = packageName;
        app.appName = getAppNameWithPackage(context, packageName);
        return app;
    }

    @NonNull
    public static Spanned html(@NonNull Context context, @StringRes int res) {
        final String string = context.getString(res);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(string);
        }
    }

    @NonNull
    public static Spanned html(@NonNull Context context, @NonNull String string) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(string);
        }
    }

    public static void doAfterLayout(@NonNull final View view, @NonNull final Runnable end) {
        view.requestLayout();
        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    end.run();
                }
            });
        }
    }

    public static boolean isOverlayGranted(@NonNull Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void openDrawOverlaySettings(@NonNull Activity activity) {
        try {
            Toast.makeText(activity, activity.getString(R.string.web_head_permission_toast), LENGTH_LONG).show();
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, 0);
        } catch (Exception e) {
            Timber.e(e);
            Toast.makeText(activity, R.string.overlay_missing, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isValidFavicon(@Nullable Bitmap favicon) {
        return favicon != null && !(favicon.getWidth() == 16 || favicon.getHeight() == 16
                || favicon.getWidth() == 32 || favicon.getHeight() == 32);
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


    public static Observable<Boolean> deleteCache(@NonNull Context context) {
        return Observable.fromCallable(new Callable<Boolean>() {
            boolean delete(Context context) {
                boolean deleted = true;
                try {
                    final File internalCache = context.getCacheDir();
                    if (internalCache != null && internalCache.isDirectory()) {
                        deleted = deleteDir(internalCache);
                    }
                    final File externalCache = context.getExternalCacheDir();
                    if (externalCache != null && externalCache.isDirectory()) {
                        deleted = deleteDir(externalCache);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    return false;
                }
                return deleted;
            }

            boolean deleteDir(final File dir) {
                if (dir != null && dir.isDirectory()) {
                    String[] children = dir.list();
                    for (String path : children) {
                        boolean success = deleteDir(new File(dir, path));
                        if (!success) {
                            return false;
                        }
                    }
                }
                return dir != null && dir.delete();
            }

            @Override
            public Boolean call() throws Exception {
                return delete(context.getApplicationContext());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> Timber.d("Cache deletion %b", result));
    }
}
