package arun.com.chromer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import arun.com.chromer.R;

/**
 * Created by Arun on 05/01/2016.
 */
public class PrefUtil {
    public static final String PREFERRED_PACKAGE = "preferred_package";
    public static final String TOOLBAR_COLOR = "toolbar_color";
    public static final String TOOLBAR_COLOR_PREF = "toolbar_color_pref";
    public static final String SHOW_TITLE_PREF = "title_pref";
    public static final String ENABLE_ANIMATION = "animations_pref";
    public static final String ANIMATION_TYPE = "animation_preference";
    public static final String FIRST_RUN = "firstrun";

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static boolean isFirstRun(Context context) {
        if (preferences(context).getBoolean(FIRST_RUN, true)) {
            preferences(context).edit().putBoolean(FIRST_RUN, false).apply();
            return true;
        }
        return false;
    }

    public static boolean isColoredToolbar(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(TOOLBAR_COLOR_PREF, true);
    }

    public static int getToolbarColor(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(TOOLBAR_COLOR,
                        ContextCompat.getColor(context, R.color.colorPrimary));
    }

    public static void setToolbarColor(Context context, int selectedColor) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putInt("toolbar_color", selectedColor).apply();
    }

    public static boolean isAnimationEnabled(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(ENABLE_ANIMATION, true);
    }

    public static int getAnimationPref(Context context) {
        return Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(ANIMATION_TYPE, "1"));
    }

    public static boolean isShowTitle(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(SHOW_TITLE_PREF, true);
    }
}
