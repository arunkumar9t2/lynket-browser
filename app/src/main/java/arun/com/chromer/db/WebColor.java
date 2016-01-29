package arun.com.chromer.db;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Arun on 25/01/2016.
 */
public class WebColor extends SugarRecord {
    @Unique
    String url;
    int color;

    public WebColor() {

    }

    public WebColor(String url, int color) {
        this.url = url;
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
