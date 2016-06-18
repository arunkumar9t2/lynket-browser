package arun.com.chromer.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

import timber.log.Timber;

/**
 * Created by Arun on 29/03/2015.
 */
public class FontCache {
    public static final String ROBOTO_MONO = "RobotoMono-Medium.ttf";

    private static final Hashtable<String, Typeface> fontCache = new Hashtable<>();

    public static Typeface get(String name, Context context) {
        Typeface tf = fontCache.get(name);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
            } catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }

    public static void dropCache() {
        fontCache.clear();
        Timber.d("Font cache dropped");
    }
}