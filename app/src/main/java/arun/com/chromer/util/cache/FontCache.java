package arun.com.chromer.util.cache;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * Created by Arunkumar on 06-03-2017.
 */
public class FontCache {
    private static Hashtable<String, Typeface> fontCache = new Hashtable<>();

    public static String MONO = "RobotoMono-Regular.ttf";

    public static Typeface get(String name, Context context) {
        Typeface tf = fontCache.get(name);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getApplicationContext().getAssets(), "fonts/" + name);
            } catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}