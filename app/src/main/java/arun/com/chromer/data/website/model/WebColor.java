package arun.com.chromer.data.website.model;

import android.support.annotation.ColorInt;

public class WebColor {
    public String host;
    @ColorInt
    public int color;

    WebColor() {

    }

    public WebColor(String host, int color) {
        this.host = host;
        this.color = color;
    }
}
