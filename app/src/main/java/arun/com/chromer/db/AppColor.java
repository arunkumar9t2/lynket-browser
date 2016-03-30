package arun.com.chromer.db;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Arun on 25/01/2016.
 */
@SuppressWarnings("ALL")
public class AppColor extends SugarRecord {
    @Unique
    String app;
    int color;

    public AppColor() {

    }

    public AppColor(String app, int color) {
        this.app = app;
        this.color = color;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
