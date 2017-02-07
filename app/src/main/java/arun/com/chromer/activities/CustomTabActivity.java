package arun.com.chromer.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;

import arun.com.chromer.R;
import arun.com.chromer.customtabs.CustomTabs;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.webheads.helper.WebSite;
import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;
import timber.log.Timber;

import static android.content.Intent.EXTRA_TEXT;
import static arun.com.chromer.shared.Constants.ACTION_MINIMIZE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_WEBHEAD;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;
import static arun.com.chromer.shared.Constants.NO_COLOR;

public class CustomTabActivity extends AppCompatActivity {
    private boolean isLoaded = false;
    private ExtractionTask mExtractionTask;
    private String mBaseUrl = "";
    private BroadcastReceiver mMinimizeReceiver;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBaseUrl = getIntent().getDataString();
        final boolean isWebhead = getIntent().getBooleanExtra(EXTRA_KEY_FROM_WEBHEAD, false);

        final WebSite webSite = getIntent().getParcelableExtra(EXTRA_KEY_WEBSITE);
        final int color = webSite != null && !TextUtils.isEmpty(webSite.faviconUrl) ? webSite.themeColor() : NO_COLOR;

        CustomTabs.from(this)
                .forUrl(mBaseUrl)
                .forWebHead(isWebhead)
                .fallbackColor(color)
                // .noAnimations(Preferences.aggressiveLoading(this))
                .prepare()
                .launch();

        if (Preferences.aggressiveLoading(this)) {
            delayedGoToBack();
        }

        dispatchDescriptionTask(webSite);
        registerMinimizeReceiver();
    }

    private void registerMinimizeReceiver() {
        mMinimizeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Minimize called with %s : %s", mBaseUrl, intent.toString());
                if (intent.getAction().equalsIgnoreCase(ACTION_MINIMIZE) && intent.hasExtra(EXTRA_TEXT)) {
                    if (mBaseUrl.equalsIgnoreCase(intent.getStringExtra(EXTRA_TEXT))) {
                        Timber.d("Minimized %s", intent.getStringExtra(EXTRA_TEXT));
                        moveTaskToBack(true);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mMinimizeReceiver, new IntentFilter(ACTION_MINIMIZE));
    }

    private void delayedGoToBack() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveTaskToBack(true);
            }
        }, 650);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void dispatchDescriptionTask(@Nullable final WebSite webSite) {
        /*if (Utils.isLollipopAbove()) {
            if (webSite != null && webSite.title != null) {
                final String title = webSite.title;
                final String faviconUrl = webSite.faviconUrl;
                setTaskDescription(new ActivityManager.TaskDescription(title, null, webSite.themeColor));
                Glide.with(this)
                        .load(faviconUrl)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                setTaskDescription(new ActivityManager.TaskDescription(title, resource, webSite.themeColor));
                            }
                        });
            } else {
                mExtractionTask = new ExtractionTask(mBaseUrl);
                mExtractionTask.execute();
            }
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoaded) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExtractionTask != null && !mExtractionTask.isCancelled()) {
            mExtractionTask.cancel(true);
        }
        mExtractionTask = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMinimizeReceiver);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isLoaded = true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class ExtractionTask extends AsyncTask<Void, String, Void> {
        final String mUrl;
        String mTitle;
        Bitmap mIcon;
        int color = NO_COLOR;

        ExtractionTask(@Nullable String url) {
            mUrl = url;
        }

        @Override
        protected void onPreExecute() {
            if (mUrl != null && mUrl.length() > 0) {
                setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.loading)));
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mUrl != null && mUrl.length() > 0) {
                Timber.d("Beginning extraction");
                try {
                    final HtmlFetcher fetcher = new HtmlFetcher();
                    final String unShortenedUrl = fetcher.unShortenUrl(mUrl);
                    final JResult res = fetcher.fetchAndExtract(unShortenedUrl, false);
                    mTitle = res.getTitle();

                    mIcon = Glide.with(CustomTabActivity.this)
                            .load(res.getFaviconUrl())
                            .asBitmap()
                            .into(-1, -1)
                            .get();

                    final Palette palette = Palette.from(mIcon)
                            .clearFilters()
                            .generate();
                    color = ColorUtil.getBestFaviconColor(palette);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String label = "";
            if (mTitle != null && mTitle.length() > 0) {
                label = mTitle;
            } else {
                try {
                    label = new URL(mUrl).getHost().toUpperCase();
                } catch (MalformedURLException ignored) {
                }
            }
            if (label.trim().length() == 0 && mUrl != null) {
                label = mUrl.toUpperCase();
            }
            Timber.d("Setting task description %s", label);
            if (mIcon != null && mIcon.getWidth() < 0) {
                mIcon = null;
            }
            if (color != NO_COLOR) {
                setTaskDescription(new ActivityManager.TaskDescription(label, mIcon, color));
            } else {
                setTaskDescription(new ActivityManager.TaskDescription(label, mIcon));
            }
            mIcon = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Timber.d("Cancelled");
        }
    }
}
