package arun.com.chromer.model;

import com.afollestad.inquiry.annotations.Column;

/**
 * Created by Arun on 06/01/2016.
 */
public class WebColor {
    @Column(name = "host", primaryKey = true)
    public String host;

    @Column(name = "toolbar_color")
    public int toolbarColor;

}
