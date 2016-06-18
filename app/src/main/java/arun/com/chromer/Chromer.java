package arun.com.chromer;

import android.app.Application;

import com.orm.SugarContext;

import arun.com.chromer.util.FontCache;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class Chromer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
        FontCache.dropCache();
    }
}
