package arun.com.chromer;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.orm.SugarContext;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class Chromer extends Application {
    private static OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initializeWithDefaults(this);
        }

        buildOkHttpClient();

        SugarContext.init(this);
    }

    /**
     * Returns one time initialized instance of {@link OkHttpClient}
     *
     * @return OkHttpClient
     */
    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    private void buildOkHttpClient() {
        Timber.d("Initializing OkHttpClient");
        final int cacheSize = 30 * 1024 * 1024; // 20 MiB
        final File cacheDir = new File(getCacheDir(), "http");
        okHttpClient = new OkHttpClient.Builder()
                .cache(new Cache(cacheDir, cacheSize))
                .build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
