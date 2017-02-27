package arun.com.chromer.activities.browsing.article;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Arunkumar on 26-02-2017.
 */
public class ArticleLauncher {
    private Context context;
    Uri uri;
    private ArticleIntent.Builder builder;

    private boolean newTab;

    public ArticleLauncher(final Context context, final Uri uri) {
        this.context = context;
        this.uri = uri;
        builder = new ArticleIntent.Builder(context);
    }

    public static ArticleLauncher from(@NonNull final Context context, final Uri uri) {
        return new ArticleLauncher(context, uri);
    }

    public ArticleLauncher forNewTab(boolean newTab) {
        this.newTab = newTab;
        return this;
    }

    public ArticleLauncher applyCustomizations() {
        final int color = Preferences.get(context).isColoredToolbar()
                ? Preferences.get(context).toolbarColor()
                : ContextCompat.getColor(context, R.color.colorPrimary);
        builder.setToolbarColor(color)
                .setAccentColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setTheme(ArticleIntent.THEME_DARK)
                .setTextSize(15);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void launch() {
        final Bundle extras = builder.getExtras();
        final Intent intent = new Intent(context, ChromerArticleActivity.class);
        intent.setData(uri);
        intent.putExtras(extras);
        if (!(context instanceof Activity)) {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        if (newTab || Preferences.get(context).mergeTabs()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        context.startActivity(intent);
        context = null;
    }
}

