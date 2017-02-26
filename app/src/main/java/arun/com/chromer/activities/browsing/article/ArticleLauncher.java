package arun.com.chromer.activities.browsing.article;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import xyz.klinker.android.article.ArticleIntent;

import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

/**
 * Created by Arunkumar on 26-02-2017.
 */
public class ArticleLauncher {
    private Activity activity;
    Uri uri;
    private ArticleIntent.Builder builder;

    private boolean newTab;

    public ArticleLauncher(final Activity activity, final Uri uri) {
        this.activity = activity;
        this.uri = uri;
        builder = new ArticleIntent.Builder(activity);
    }

    public static ArticleLauncher from(@NonNull final Activity activity, final Uri uri) {
        return new ArticleLauncher(activity, uri);
    }

    public ArticleLauncher forNewTab(boolean newTab) {
        this.newTab = newTab;
        return this;
    }

    public ArticleLauncher applyCustomizations() {
        final int color = Preferences.get(activity).isColoredToolbar()
                ? Preferences.get(activity).toolbarColor()
                : ContextCompat.getColor(activity, R.color.colorPrimary);
        builder.setToolbarColor(color)
                .setAccentColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                .setTheme(ArticleIntent.THEME_DARK)
                .setTextSize(15);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void launch() {
        final Bundle extras = builder.getExtras();
        final Intent intent = new Intent(activity, ChromerArticleActivity.class);
        intent.setData(uri);
        intent.putExtras(extras);
        if (newTab || Preferences.get(activity).mergeTabs()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        activity.startActivity(intent);
        activity = null;
    }
}

