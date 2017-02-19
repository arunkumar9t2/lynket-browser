package arun.com.chromer.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.activities.settings.preferences.manager.Preferences;
import arun.com.chromer.webheads.WebHeadService;
import arun.com.chromer.webheads.helper.WebSite;
import arun.com.chromer.webheads.ui.views.WebHead;
import timber.log.Timber;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Intent.EXTRA_TEXT;
import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static arun.com.chromer.shared.Constants.ACTION_CLOSE_ROOT;
import static arun.com.chromer.shared.Constants.ACTION_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_WEBHEAD;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;

/**
 * Created by Arun on 03/09/2016.
 */
public class DocumentUtils {

    @TargetApi(LOLLIPOP)
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
    public static void smartOpenNewTab(@NonNull final Context context, @NonNull final WebHead webHead) {
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
    public static void smartOpenNewTab(@NonNull final Context context, @NonNull final WebSite webSite) {
        if (!reOrderCustomTabByUrls(context, webSite.url, webSite.preferredUrl())) {
            openNewCustomTab(context, webSite);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @TargetApi(LOLLIPOP)
    private static boolean reOrderCustomTabByUrls(@NonNull final Context context, @NonNull final String shortUrl, @Nullable final String longUrl) {
        if (!Preferences.mergeTabs(context)) {
            return false;
        }
        final ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
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

    @TargetApi(LOLLIPOP)
    public static void openNewCustomTab(@NonNull final Context context, @NonNull final WebSite webSite) {
        final Intent customTabActivity = new Intent(context, CustomTabActivity.class);
        customTabActivity.setData(Uri.parse(webSite.url));
        customTabActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        if (Preferences.mergeTabs(context)) {
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            customTabActivity.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        customTabActivity.putExtra(EXTRA_KEY_FROM_WEBHEAD, true);
        customTabActivity.putExtra(EXTRA_KEY_WEBSITE, webSite);
        context.startActivity(customTabActivity);
    }

    @TargetApi(LOLLIPOP)
    public static void openNewCustomTab(@NonNull final Context context, @NonNull final WebHead webHead) {
        final Intent customTabActivity = new Intent(context, CustomTabActivity.class);
        customTabActivity.setData(Uri.parse(webHead.getUrl()));
        customTabActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        if (webHead.isFromNewTab() || Preferences.mergeTabs(context)) {
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            customTabActivity.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        customTabActivity.putExtra(EXTRA_KEY_FROM_WEBHEAD, true);
        customTabActivity.putExtra(EXTRA_KEY_WEBSITE, webHead.getWebsite());
        context.startActivity(customTabActivity);
    }

    @TargetApi(LOLLIPOP)
    public static void finishTaskByUrl(@NonNull Context context, @Nullable String url) {
        if (url == null) return;
        final ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
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
        final Intent minimizeIntent = new Intent(ACTION_MINIMIZE);
        minimizeIntent.putExtra(EXTRA_TEXT, url);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(minimizeIntent);
        // Open a new web head if the old was destroyed
        if (Preferences.webHeads(context)) {
            final Intent webHeadService = new Intent(context, WebHeadService.class);
            webHeadService.addFlags(FLAG_ACTIVITY_NEW_TASK);
            webHeadService.setData(Uri.parse(url));
            webHeadService.putExtra(EXTRA_KEY_MINIMIZE, true);
            context.startService(webHeadService);
        }
    }

    public static void closeRootActivity(@NonNull Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_CLOSE_ROOT));
    }
}
