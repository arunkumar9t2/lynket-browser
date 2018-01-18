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

package arun.com.chromer.browsing.article;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.browsing.article.view.ElasticDragDismissFrameLayout;
import arun.com.chromer.data.webarticle.WebArticleRepository;
import arun.com.chromer.data.webarticle.model.WebArticle;
import arun.com.chromer.shared.base.activity.BaseActivity;
import arun.com.chromer.util.glide.GlideApp;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public abstract class BaseArticleActivity extends BaseActivity {
    private static final String TAG = BaseArticleActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final int MIN_NUM_ELEMENTS = 1;

    private String url;

    private int primaryColor;
    private int accentColor;


    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    WebArticleRepository webArticleRepository;

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;

    private final View.OnClickListener sideClickListener = view -> finish();
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private ArticleScrollListener articleScrollListener;

    @Override
    protected int getLayoutRes() {
        return R.layout.article_activity_article;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getDataString();

        if (DEBUG) {
            Log.v(TAG, "loading article: " + url);
        }
        setDayNightTheme();
        readCustomizations();

        toolbar = findViewById(R.id.article_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.article_ic_close);
            getSupportActionBar().setTitle(null);
        }

        final View statusBar = findViewById(R.id.article_status_bar);
        recyclerView = findViewById(R.id.article_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        articleScrollListener = new ArticleScrollListener(toolbar, statusBar, primaryColor);
        recyclerView.addOnScrollListener(articleScrollListener);

        progressBar = findViewById(R.id.article_loading);

        findViewById(R.id.article_transparent_side_1).setOnClickListener(sideClickListener);
        findViewById(R.id.article_transparent_side_2).setOnClickListener(sideClickListener);

        final ElasticDragDismissFrameLayout dragDismissLayout = findViewById(R.id.article_drag_dismiss_layout);
        dragDismissLayout.addListener(new ElasticDragDismissFrameLayout.ElasticDragDismissCallback() {
            @Override
            public void onDragDismissed() {
                super.onDragDismissed();
                finish();
            }
        });

        final Subscription subscription = webArticleRepository
                .getWebArticle(url)
                .doOnError(this::onArticleLoadingFailed)
                .subscribe(webArticle -> {
                    if (webArticle == null) {
                        onArticleLoadingFailed(null);
                    } else {
                        onArticleLoaded(webArticle);
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.clear();
    }

    private void setPrimaryColor(@ColorInt int primaryColor) {
        this.primaryColor = primaryColor;
        ArticleUtil.changeRecyclerOverscrollColors(recyclerView, primaryColor);
        ArticleUtil.changeProgressBarColors(progressBar, primaryColor);
        articleScrollListener.setPrimaryColor(primaryColor);
    }

    private void readCustomizations() {
        primaryColor = getIntent().getIntExtra(ArticleIntent.EXTRA_TOOLBAR_COLOR,
                ContextCompat.getColor(this, R.color.article_colorPrimary));
        accentColor = getIntent().getIntExtra(ArticleIntent.EXTRA_ACCENT_COLOR,
                ContextCompat.getColor(this, R.color.article_colorAccent));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDayNightTheme() {
        int theme = getIntent().getIntExtra(ArticleIntent.EXTRA_THEME, ArticleIntent.THEME_AUTO);
        if (theme == ArticleIntent.THEME_LIGHT) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme == ArticleIntent.THEME_DARK) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.article_windowBackground));
            }
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }
    }

    protected void onArticleLoadingFailed(@Nullable Throwable throwable) {
        progressBar.setVisibility(View.GONE);
    }

    protected void onArticleLoaded(@NonNull WebArticle webArticle) {
        if (webArticle.elements != null && webArticle.elements.size() >= MIN_NUM_ELEMENTS) {
            renderArticle(webArticle);
            applyTaskDescriptionAndColors(webArticle);
        } else {
            onArticleLoadingFailed(null);
        }
    }

    private void applyTaskDescriptionAndColors(@NonNull final WebArticle webArticle) {
        final String title = webArticle.safeLabel();
        final String faviconUrl = webArticle.faviconUrl;
        final int color = webArticle.themeColor() == -1 ? primaryColor : webArticle.themeColor();
        setPrimaryColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(title, null, color));
            GlideApp.with(this)
                    .asBitmap()
                    .load(faviconUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            setTaskDescription(new ActivityManager.TaskDescription(title, resource, color));
                        }
                    });
        }
    }

    private void renderArticle(@NonNull WebArticle webArticle) {
        adapter = new ArticleAdapter(webArticle, accentColor);
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
        adapter.addElements(webArticle.elements);
    }
}
