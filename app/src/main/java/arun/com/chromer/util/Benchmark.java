package arun.com.chromer.util;

import android.support.annotation.Nullable;

import arun.com.chromer.BuildConfig;
import timber.log.Timber;

/**
 * Created by Arun on 30/06/2016.
 */
public class Benchmark {
    private static long startNs;
    private static String process;

    private Benchmark() {
        throw new AssertionError();
    }

    @SuppressWarnings("SameParameterValue")
    public static void start(@Nullable String name) {
        startNs = System.nanoTime();
        process = name != null ? name : "";
    }

    public static void end() {
        long endNs = System.nanoTime();
        long duration = (endNs - startNs);
        if (BuildConfig.DEBUG) {
            Timber.i("%s took : %d ms", process, duration / 1000000);
        }
    }
}
