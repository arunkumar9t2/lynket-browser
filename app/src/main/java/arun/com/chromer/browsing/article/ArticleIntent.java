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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Class holding the {@link Intent} and start bundle for an Article Activity.
 * <p>
 * <p>
 * <strong>Note:</strong> The constants below are public for the browser implementation's benefit.
 * You are strongly encouraged to use {@link ArticleIntent.Builder}.</p>
 */
public final class ArticleIntent {
    /**
     * Extra that changes the background color for the toolbar. colorRes is an int that specifies a
     * {@link Color}, not a resource id.
     */
    public static final String EXTRA_TOOLBAR_COLOR = "EXTRA_TOOLBAR_COLOR";
    /**
     * Extra that changes the accent color for the activity. colorRes is an int that specifies a
     * {@link Color}, not a resource id.
     */
    public static final String EXTRA_ACCENT_COLOR = "xyz.klinker.android.article.extra.EXTRA_ACCENT_COLOR";
    /**
     * Extra that changes the text size for the article. The default is 15 sp.
     */
    public static final String EXTRA_TEXT_SIZE = "xyz.klinker.android.article.extra.EXTRA_TEXT_SIZE";
    /**
     * Integer for the theme you want to use, choose between {@link #THEME_LIGHT},
     * {@link #THEME_DARK} and {@link #THEME_AUTO}.
     */
    public static final String EXTRA_THEME = "xyz.klinker.android.article.extra.EXTRA_THEME";
    /**
     * Use the light theme.
     */
    public static final int THEME_LIGHT = 1;
    /**
     * Use the dark theme.
     */
    public static final int THEME_DARK = 2;
    /**
     * Use an automatic theme, changing between light and dark based on time of day.
     */
    public static final int THEME_AUTO = 3;
    /**
     * An {@link Intent} used to start the Article activity.
     */
    @NonNull
    public final Intent intent;

    private ArticleIntent(@NonNull Intent intent) {
        this.intent = intent;
    }

    /**
     * Convenience method to launch a Custom Tabs Activity.
     *
     * @param activity The source Context.
     * @param uri      The URL to load in the Custom Tab.
     */
    public void launchUrl(@NonNull Activity activity, @NonNull Uri uri) {
        intent.setData(uri);
        ActivityCompat.startActivity(activity, intent, null);
    }

    /**
     * Builder class for {@link ArticleIntent} objects.
     */
    public static final class Builder {
        private final Intent articleIntent;

        public Builder(Context context) {
            articleIntent = new Intent(context, BaseArticleActivity.class);
        }

        /**
         * Sets the toolbar color.
         *
         * @param color {@link Color}
         */
        public ArticleIntent.Builder setToolbarColor(@ColorInt int color) {
            articleIntent.putExtra(EXTRA_TOOLBAR_COLOR, color);
            return this;
        }

        /**
         * Sets the accent color.
         *
         * @param color {@link Color}
         */
        public ArticleIntent.Builder setAccentColor(@ColorInt int color) {
            articleIntent.putExtra(EXTRA_ACCENT_COLOR, color);
            return this;
        }

        public ArticleIntent.Builder setTextSize(int spSize) {
            articleIntent.putExtra(EXTRA_TEXT_SIZE, spSize);
            return this;
        }

        /**
         * Sets the theme to use.
         *
         * @param theme One of {@link #THEME_LIGHT}, {@link #THEME_DARK} or {@link #THEME_AUTO}.
         */
        public ArticleIntent.Builder setTheme(int theme) {
            articleIntent.putExtra(EXTRA_THEME, theme);
            return this;
        }

        /**
         * Combines all the options that have been set and returns a new {@link ArticleIntent}
         * object.
         */
        public ArticleIntent build() {
            return new ArticleIntent(articleIntent);
        }

        /**
         * Returns all extras added by this builder. This is useful when you have extended
         * {@link BaseArticleActivity} and would like to use default customizations provided by the API.
         */
        @NonNull
        public Bundle getExtras() {
            return articleIntent.getExtras();
        }
    }
}
