/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.R;
import arun.com.chromer.browsing.customtabs.CustomTabs;
import arun.com.chromer.util.Utils;

import static arun.com.chromer.shared.Constants.CHROME_PACKAGE;
import static java.lang.Integer.parseInt;

/**
 * Created by Arun on 05/01/2016.
 */
@Singleton
public class Preferences {
    public static final String TOOLBAR_COLOR = "toolbar_color";
    public static final String WEB_HEADS_COLOR = "webhead_color";
    public static final String ANIMATION_TYPE = "animation_preference";
    public static final String ANIMATION_SPEED = "animation_speed_preference";
    public static final String DYNAMIC_COLOR = "dynamic_color";
    public static final String WEB_HEAD_CLOSE_ON_OPEN = "webhead_close_onclick_pref";
    public static final String PREFERRED_ACTION = "preferred_action_preference";
    public static final String WEB_HEAD_ENABLED = "webhead_enabled_pref";
    public static final String WEB_HEAD_SPAWN_LOCATION = "webhead_spawn_preference";
    public static final String WEB_HEAD_SIZE = "webhead_size_preference";
    public static final String BOTTOM_BAR_ENABLED = "bottombar_enabled_preference";
    public static final int PREFERRED_ACTION_BROWSER = 1;
    public static final int PREFERRED_ACTION_FAV_SHARE = 2;
    public static final int PREFERRED_ACTION_GEN_SHARE = 3;
    public static final int ANIMATION_MEDIUM = 1;
    public static final int ANIMATION_SHORT = 2;
    public static final String TOOLBAR_COLOR_PREF = "toolbar_color_pref";
    public static final String WARM_UP = "warm_up_preference";
    public static final String PRE_FETCH = "pre_fetch_preference";
    public static final String WIFI_PREFETCH = "wifi_preference";
    public static final String PRE_FETCH_NOTIFICATION = "pre_fetch_notification_preference";
    public static final String PER_APP_PREFERENCE_DUMMY = "blacklist_preference_dummy";
    public static final String MERGE_TABS_AND_APPS = "merge_tabs_and_apps_preference";
    public static final String AGGRESSIVE_LOADING = "aggressive_loading";
    public static final String PREFERRED_CUSTOM_TAB_PACKAGE = "preferred_package";
    public static final String DYNAMIC_COLOR_APP = "dynamic_color_app";
    public static final String DYNAMIC_COLOR_WEB = "dynamic_color_web";
    public static final String AMP_MODE = "amp_mode_pref";
    public static final String ARTICLE_MODE = "article_mode_pref";
    public static final String ARTICLE_THEME = "article_theme_preference";
    public static final int THEME_DARK = 1;
    public static final int THEME_LIGHT = 2;
    public static final int THEME_AUTO = 3;
    public static final int THEME_BLACK = 4;
    public static final String INCOGNITO_MODE = "incognito_mode_pref";
    public static final String FULL_INCOGNITO_MODE = "full_incognito_mode";
    public static final String ARTICLE_TEXT_SIZE = "article_text_size_pref";
    public static final String USE_WEBVIEW_PREF = "use_webview_pref";
    public static final String MINIMIZE_BEHAVIOR_PREFERENCE = "minimize_behavior_preference";
    public static final String MINIMIZE_BEHAVIOR_PREFERENCE_KEY = "2";
    private static final String WEB_HEAD_FAVICON = "webhead_favicons_pref";
    private static final String PER_APP_SETTINGS = "blacklist_preference";
    private static final String FIRST_RUN = "firstrun_3";
    private static final String SECONDARY_PREF = "secondary_preference";
    private static final String FAV_SHARE_PREF = "fav_share_preference";
    // Singleton instance
    private static Preferences INSTANCE;

    private final Context context;

    @Inject
    public Preferences(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized Preferences get(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Preferences(context);
        }
        return INSTANCE;
    }

    /**
     * Returns default shared preferences.
     *
     * @return {@link SharedPreferences} instance
     */
    @NonNull
    public SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isFirstRun() {
        if (getDefaultSharedPreferences().getBoolean(FIRST_RUN, true)) {
            getDefaultSharedPreferences().edit().putBoolean(FIRST_RUN, false).apply();
            return true;
        }
        return false;
    }

    public boolean isColoredToolbar() {
        return getDefaultSharedPreferences().getBoolean(TOOLBAR_COLOR_PREF, true);
    }

    public int toolbarColor() {
        return getDefaultSharedPreferences().getInt(TOOLBAR_COLOR, ContextCompat.getColor(context, R.color.colorPrimary));
    }

    public void toolbarColor(int selectedColor) {
        getDefaultSharedPreferences().edit().putInt(TOOLBAR_COLOR, selectedColor).apply();
    }

    public int webHeadColor() {
        return getDefaultSharedPreferences().getInt(WEB_HEADS_COLOR, ContextCompat.getColor(context, R.color.web_head_color));
    }

    public void webHeadColor(int selectedColor) {
        getDefaultSharedPreferences().edit().putInt(WEB_HEADS_COLOR, selectedColor).apply();
    }

    public boolean isAnimationEnabled() {
        return animationType() != 0;
    }

    public int animationType() {
        return parseInt(getDefaultSharedPreferences().getString(ANIMATION_TYPE, "1"));
    }

    public int animationSpeed() {
        return parseInt(getDefaultSharedPreferences().getString(ANIMATION_SPEED, "1"));
    }

    public int articleTheme() {
        return parseInt(getDefaultSharedPreferences().getString(ARTICLE_THEME, "1"));
    }

    public int preferredAction() {
        return parseInt(getDefaultSharedPreferences().getString(PREFERRED_ACTION, "1"));
    }

    @Nullable
    public String customTabPackage() {
        String packageName = getDefaultSharedPreferences().getString(PREFERRED_CUSTOM_TAB_PACKAGE, null);
        if (packageName != null && Utils.isPackageInstalled(context, packageName))
            return packageName;
        else {
            packageName = getDefaultCustomTabApp();
            // update the new custom tab package
            customTabPackage(packageName);
        }
        return packageName;
    }

    @Nullable
    public String getDefaultCustomTabApp() {
        if (CustomTabs.isPackageSupportCustomTabs(context, CHROME_PACKAGE))
            return CHROME_PACKAGE;
        final List<String> supportingPackages = CustomTabs.getCustomTabSupportingPackages(context);
        if (supportingPackages.size() > 0) {
            return supportingPackages.get(0);
        } else
            return null;
    }

    public void customTabPackage(String string) {
        useWebView(false);
        getDefaultSharedPreferences().edit().putString(PREFERRED_CUSTOM_TAB_PACKAGE, string).apply();
    }

    @Nullable
    public String secondaryBrowserComponent() {
        return getDefaultSharedPreferences().getString(SECONDARY_PREF, null);
    }

    public void secondaryBrowserComponent(final String componentString) {
        getDefaultSharedPreferences().edit().putString(SECONDARY_PREF, componentString).apply();
    }

    @Nullable
    public String secondaryBrowserPackage() {
        final String flatString = secondaryBrowserComponent();
        if (flatString == null) {
            return null;
        }
        final ComponentName cN = ComponentName.unflattenFromString(flatString);
        if (cN == null) return null;
        return cN.getPackageName();
    }

    @Nullable
    public String favShareComponent() {
        return getDefaultSharedPreferences().getString(FAV_SHARE_PREF, null);
    }

    public void favShareComponent(final String componentString) {
        getDefaultSharedPreferences().edit().putString(FAV_SHARE_PREF, componentString).apply();
    }

    @Nullable
    public String favSharePackage() {
        final String flatString = favShareComponent();
        if (flatString == null) {
            return null;
        }
        final ComponentName cN = ComponentName.unflattenFromString(flatString);
        if (cN == null) return null;
        return cN.getPackageName();
    }

    public boolean warmUp() {
        return getDefaultSharedPreferences().getBoolean(WARM_UP, false);
    }

    public void warmUp(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(WARM_UP, preference).apply();
    }

    public boolean preFetch() {
        return getDefaultSharedPreferences().getBoolean(PRE_FETCH, false);
    }

    public boolean ampMode() {
        return getDefaultSharedPreferences().getBoolean(AMP_MODE, false);
    }

    public void ampMode(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(AMP_MODE, preference).apply();
    }

    public boolean articleMode() {
        return getDefaultSharedPreferences().getBoolean(ARTICLE_MODE, false);
    }

    public void articleMode(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(ARTICLE_MODE, preference).apply();
    }

    public boolean historyDisabled() {
        return getDefaultSharedPreferences().getBoolean(INCOGNITO_MODE, false);
    }

    public void historyDisabled(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(INCOGNITO_MODE, preference).apply();
    }

    public void fullIncognitoMode(boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(FULL_INCOGNITO_MODE, preference).apply();
    }

    public boolean fullIncognitoMode() {
        return getDefaultSharedPreferences().getBoolean(FULL_INCOGNITO_MODE, false);
    }

    public void useWebView(boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(USE_WEBVIEW_PREF, preference).apply();
    }

    public boolean useWebView() {
        return getDefaultSharedPreferences().getBoolean(USE_WEBVIEW_PREF, false);
    }

    public void minimizeToWebHead(boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(MINIMIZE_BEHAVIOR_PREFERENCE, preference).apply();
    }

    public boolean minimizeToWebHead() {
        return getDefaultSharedPreferences()
                .getString(MINIMIZE_BEHAVIOR_PREFERENCE, "1")
                .equals(MINIMIZE_BEHAVIOR_PREFERENCE_KEY);
    }

    public void articleTextSizeIncrement(int increment) {
        getDefaultSharedPreferences().edit().putInt(ARTICLE_TEXT_SIZE, increment).apply();
    }

    public int articleTextSizeIncrement() {
        return getDefaultSharedPreferences().getInt(ARTICLE_TEXT_SIZE, 0);
    }

    public void preFetch(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(PRE_FETCH, preference).apply();
    }

    public boolean wifiOnlyPrefetch() {
        return getDefaultSharedPreferences().getBoolean(WIFI_PREFETCH, false);
    }

    public void wifiOnlyPrefetch(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(WIFI_PREFETCH, preference).apply();
    }

    public boolean preFetchNotification() {
        return getDefaultSharedPreferences().getBoolean(PRE_FETCH_NOTIFICATION, true);
    }

    public void preFetchNotification(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(PRE_FETCH_NOTIFICATION, preference).apply();
    }

    public boolean dynamicToolbar() {
        return getDefaultSharedPreferences().getBoolean(DYNAMIC_COLOR, false);
    }

    public boolean dynamiceToolbarEnabledAndWebEnabled() {
        return dynamicToolbar() && dynamicToolbarOnWeb();
    }

    public void dynamicToolbar(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(DYNAMIC_COLOR, preference).apply();
    }

    public boolean dynamicToolbarOnApp() {
        return getDefaultSharedPreferences().getBoolean(DYNAMIC_COLOR_APP, false);
    }

    private void dynamicToolbarOnApp(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(DYNAMIC_COLOR_APP, preference).apply();
    }

    public boolean dynamicToolbarOnWeb() {
        return getDefaultSharedPreferences().getBoolean(DYNAMIC_COLOR_WEB, false);
    }

    private void dynamicToolbarOnWeb(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(DYNAMIC_COLOR_WEB, preference).apply();
    }

    public boolean aggressiveLoading() {
        return Utils.ANDROID_LOLLIPOP
                && webHeads()
                && getDefaultSharedPreferences().getBoolean(AGGRESSIVE_LOADING, false);
    }

    public void aggressiveLoading(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(AGGRESSIVE_LOADING, preference).apply();
    }

    @NonNull
    public CharSequence dynamicColorSummary() {
        if (dynamicToolbarOnApp() && dynamicToolbarOnWeb()) {
            return context.getString(R.string.dynamic_summary_appweb);
        } else if (dynamicToolbarOnApp()) {
            return context.getString(R.string.dynamic_summary_app);
        } else if (dynamicToolbarOnWeb()) {
            return context.getString(R.string.dynamic_summary_web);
        } else
            return context.getString(R.string.no_option_selected);
    }

    public boolean webHeads() {
        return getDefaultSharedPreferences().getBoolean(WEB_HEAD_ENABLED, false);
    }

    public void webHeads(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(WEB_HEAD_ENABLED, preference).apply();
    }

    public boolean favicons() {
        return getDefaultSharedPreferences().getBoolean(WEB_HEAD_FAVICON, true);
    }

    public void favicons(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(WEB_HEAD_FAVICON, preference).apply();
    }

    public int webHeadsSpawnLocation() {
        return parseInt(getDefaultSharedPreferences().getString(WEB_HEAD_SPAWN_LOCATION, "1"));
    }

    public int webHeadsSize() {
        return parseInt(getDefaultSharedPreferences().getString(WEB_HEAD_SIZE, "1"));
    }

    public boolean webHeadsCloseOnOpen() {
        return getDefaultSharedPreferences().getBoolean(WEB_HEAD_CLOSE_ON_OPEN, false);
    }

    public void webHeadsCloseOnOpen(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(WEB_HEAD_CLOSE_ON_OPEN, preference).apply();
    }

    public boolean perAppSettings() {
        return getDefaultSharedPreferences().getBoolean(PER_APP_SETTINGS, false);
    }

    public void perAppSettings(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(PER_APP_SETTINGS, preference).apply();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean mergeTabs() {
        return Utils.isLollipopAbove() && getDefaultSharedPreferences().getBoolean(MERGE_TABS_AND_APPS, false);
    }

    public void mergeTabs(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(MERGE_TABS_AND_APPS, preference).apply();
    }

    public boolean bottomBar() {
        return getDefaultSharedPreferences().getBoolean(BOTTOM_BAR_ENABLED, true);
    }

    public void bottomBar(final boolean preference) {
        getDefaultSharedPreferences().edit().putBoolean(BOTTOM_BAR_ENABLED, preference).apply();
    }

    public boolean isAppBasedToolbar() {
        return dynamicToolbarOnApp() && dynamicToolbar();
    }
}
