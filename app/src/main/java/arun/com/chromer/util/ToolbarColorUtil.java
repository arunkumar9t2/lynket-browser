package arun.com.chromer.util;

import android.support.annotation.ColorInt;

import com.afollestad.inquiry.Inquiry;

import arun.com.chromer.model.WebColor;

/**
 * Created by Arun on 06/01/2016.
 */
public class ToolbarColorUtil {
    public static final String TABLE_COLOR = "WebToolbarColor";


    public static void insertColor(WebColor websiteToolbarColor) {
        if (getColor(websiteToolbarColor.host) == -1) {
            Inquiry.get()
                    .insertInto(TABLE_COLOR, WebColor.class)
                    .values(websiteToolbarColor)
                    .run();
        } else {
            Inquiry.get()
                    .update(TABLE_COLOR, WebColor.class)
                    .where("host = ?", websiteToolbarColor.host)
                    .values(websiteToolbarColor)
                    .run();
        }
    }

    @ColorInt
    public static int getColor(String url) {
        WebColor websiteToolbarColor = Inquiry.get()
                .selectFrom(TABLE_COLOR, WebColor.class)
                .where("host = ?", url)
                .one();
        return websiteToolbarColor != null ? websiteToolbarColor.toolbarColor : -1;
    }
}
