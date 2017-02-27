package arun.com.chromer.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.activities.browsing.article.ArticleLauncher;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.webheads.WebHeadService;
import timber.log.Timber;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static arun.com.chromer.shared.Constants.ACTION_CLOSE_ROOT;
import static arun.com.chromer.shared.Constants.ACTION_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_WEBHEAD;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_ORIGINAL_URL;
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

    public static void smartOpenNewTab(@NonNull final Context context, @NonNull final WebSite webSite) {
        smartOpenNewTab(context, webSite, false);
    }

    /**
     * Reorders a old task if it exists else opens a new tab
     *
     * @param context Context to work with
     * @param webSite Website data
     */
    public static void smartOpenNewTab(@NonNull final Context context, @NonNull final WebSite webSite, final boolean isNewTab) {
        if (!reOrderTab(context, webSite)) {
            if (Preferences.get(context).articleMode()) {
                openNewArticleTab(context, webSite, isNewTab);
            } else {
                openNewCustomTab(context, webSite, isNewTab);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @TargetApi(LOLLIPOP)
    private static boolean reOrderTab(@NonNull final Context context, @NonNull WebSite webSite) {
        if (!Preferences.get(context).mergeTabs()) {
            return false;
        }
        final ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (final ActivityManager.AppTask task : am.getAppTasks()) {
            final ActivityManager.RecentTaskInfo info = DocumentUtils.getTaskInfoFromTask(task);
            try {
                final Intent intent = info.baseIntent;
                final String url = intent.getDataString();
                if (url.equalsIgnoreCase(webSite.url)
                        || url.equalsIgnoreCase(webSite.preferredUrl())
                        || url.equalsIgnoreCase(webSite.ampUrl)) {
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
    public static void openNewCustomTab(@NonNull final Context context, @NonNull final WebSite webSite, boolean isFromNewTab) {
        final Intent customTabActivity = new Intent(context, CustomTabActivity.class);
        final Uri usableUri = getUsableUri(context, webSite);

        customTabActivity.setData(usableUri);
        customTabActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        if (isFromNewTab || Preferences.get(context).mergeTabs()) {
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            customTabActivity.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK);
        }

        customTabActivity.putExtra(EXTRA_KEY_FROM_WEBHEAD, true);
        customTabActivity.putExtra(EXTRA_KEY_WEBSITE, webSite);
        context.startActivity(customTabActivity);
    }


    public static void openNewArticleTab(Context context, WebSite webSite, boolean newTab) {
        ArticleLauncher.from(context, getUsableUri(context, webSite))
                .applyCustomizations()
                .forNewTab(newTab)
                .launch();
    }

    /**
     * Given the myraid of preferences we have, this method gives the correct Uri we should be using
     * for opening a new tab.
     *
     * @param context Context to work with.
     * @param webSite Website data.
     * @return
     */
    private static Uri getUsableUri(@NonNull Context context, @NonNull WebSite webSite) {
        if (Preferences.get(context).articleMode()) {
            return Uri.parse(webSite.url);
        } else if (Preferences.get(context).ampMode()) {
            return TextUtils.isEmpty(webSite.ampUrl) ? Uri.parse(webSite.preferredUrl()) : Uri.parse(webSite.ampUrl);
        } else {
            return Uri.parse(webSite.preferredUrl());
        }
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
        minimizeIntent.putExtra(EXTRA_KEY_ORIGINAL_URL, url);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(minimizeIntent);
        // Open a new web head if the old was destroyed
        if (Preferences.get(context).webHeads()) {
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
