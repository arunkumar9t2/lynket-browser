package arun.com.chromer;

import android.app.Application;

import com.afollestad.inquiry.Inquiry;

import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class Chromer extends Application {
    public static final String DBNAME = "Chromer_database";

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Inquiry.init(this, DBNAME, 1);
    }
}
