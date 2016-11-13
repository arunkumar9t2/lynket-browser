package arun.com.chromer.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.webheads.WebHeadService;
import arun.com.chromer.webheads.helper.WebSite;
import arun.com.chromer.webheads.ui.WebHead;
import timber.log.Timber;

/**
 * Created by Arun on 03/09/2016.
 */
public class DocumentUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ActivityManager.RecentTaskInfo getTaskInfoFromTask(ActivityManager.AppTask task) {
        ActivityManager.RecentTaskInfo info = null;
        try {
            info = task.getTaskInfo();
        } catch (IllegalArgumentException e) {
            Timber.d("Failed to retrieve task info: %s", e.toString());
        }
        return info;
    }

    /**
     * Reorders a old task if it exists else opens a new tab
     *
     * @param context
     * @param webHead
     */
    public static void smartOpenNewTab(@NonNull Context context, @NonNull WebHead webHead) {
        if (!reOrderCustomTabByUrls(context, webHead.getUrl(), webHead.getUnShortenedUrl())) {
            openNewCustomTab(context, webHead);
        }
    }

    /**
     * Reorders a old task if it exists else opens a new tab
     *
     * @param context
     * @param webSite
     */
    public static void smartOpenNewTab(@NonNull Context context, @NonNull WebSite webSite) {
        if (!reOrderCustomTabByUrls(context, webSite.url, webSite.longUrl)) {
            openNewCustomTab(context, webSite);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean reOrderCustomTabByUrls(@NonNull Context context, @NonNull String shortUrl, @Nullable String longUrl) {
        if (!Preferences.mergeTabs(context)) {
            return false;
        }
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.AppTask task : am.getAppTasks()) {
            final ActivityManager.RecentTaskInfo info = DocumentUtils.getTaskInfoFromTask(task);
            try {
                final Intent intent = info.baseIntent;
                final String url = intent.getDataString();
                if (url.equalsIgnoreCase(shortUrl) || url.equalsIgnoreCase(longUrl)) {
                    Timber.d("Moved tab to front %s", url);
                    task.moveToFront();
                    return true;
                }
            } catch (Exception ig) {
                Timber.e(ig.toString());
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void openNewCustomTab(@NonNull Context context, @NonNull WebSite webSite) {
        final Intent customTabActivity = new Intent(context, CustomTabActivity.class);
        customTabActivity.setData(Uri.parse(webSite.url));
        customTabActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Preferences.mergeTabs(context)) {
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        customTabActivity.putExtra(Constants.EXTRA_KEY_FROM_WEBHEAD, true);
        customTabActivity.putExtra(Constants.EXTRA_KEY_WEBSITE, webSite);
        context.startActivity(customTabActivity);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void openNewCustomTab(@NonNull Context context, @NonNull WebHead webHead) {
        final Intent customTabActivity = new Intent(context, CustomTabActivity.class);
        customTabActivity.setData(Uri.parse(webHead.getUnShortenedUrl()));
        customTabActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (webHead.isFromNewTab() || Preferences.mergeTabs(context)) {
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        customTabActivity.putExtra(Constants.EXTRA_KEY_FROM_WEBHEAD, true);
        customTabActivity.putExtra(Constants.EXTRA_KEY_WEBSITE, webHead.getWebsite());
        context.startActivity(customTabActivity);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void finishTaskByUrl(@NonNull Context context, @Nullable String url) {
        if (url == null) return;
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.AppTask task : am.getAppTasks()) {
            final ActivityManager.RecentTaskInfo info = DocumentUtils.getTaskInfoFromTask(task);
            try {
                final Intent intent = info.baseIntent;
                final String taskUrl = intent.getDataString();
                if (url.equalsIgnoreCase(taskUrl)) {
                    Timber.d("Removed task %s", task.toString());
                    task.finishAndRemoveTask();
                    break;
                }
            } catch (Exception ig) {
                Timber.e(ig.toString());
            }
        }
    }

    public static void minimizeTaskByUrl(@NonNull Context context, @Nullable String url) {
        Timber.d("Attempting to minimize %s", url);
        // Ask tabs to minimize
        final Intent minimizeIntent = new Intent(Constants.ACTION_MINIMIZE);
        minimizeIntent.putExtra(Intent.EXTRA_TEXT, url);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(minimizeIntent);
        // Open a new web head if the old was destroyed
        if (Preferences.webHeads(context)) {
            final Intent webHeadService = new Intent(context, WebHeadService.class);
            webHeadService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            webHeadService.setData(Uri.parse(url));
            webHeadService.putExtra(Constants.EXTRA_KEY_MINIMIZE, true);
            context.startService(webHeadService);
        }
    }
}
