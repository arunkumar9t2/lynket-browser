package arun.com.chromer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;

/**
 * Created by Arun on 17/12/2015.
 */
class Util {
    public static CustomTabsIntent getCutsomizedTabIntent(
            Context c
    ) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        if (sharedPreferences.getBoolean("toolbar_color_pref", true)) {
            builder.setToolbarColor(ContextCompat.getColor(c, R.color.colorPrimary));
        }

        if (sharedPreferences.getBoolean("animations_pref", true)) {
            builder.setStartAnimations(c, R.anim.slide_in_right, R.anim.slide_out_left)
                    .setExitAnimations(c, R.anim.slide_in_left, R.anim.slide_out_right);
        }

        if (sharedPreferences.getBoolean("url_pref", true)) {
            builder.enableUrlBarHiding();
        }

        if (sharedPreferences.getBoolean("title_pref", true)) {
            builder.setShowTitle(true);
        } else
            builder.setShowTitle(false);

        return builder.build();
    }

    public static void openPlayStore(Context context, String appPackageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
