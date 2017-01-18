package arun.com.chromer.glide;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import arun.com.chromer.util.Utils;
import timber.log.Timber;

public class AppIconModelLoader implements StreamModelLoader<String> {
    private final Context context;

    public AppIconModelLoader(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(final String packageName, int width, int height) {
        return new DataFetcher<InputStream>() {
            InputStream inputStream;

            @Override
            public InputStream loadData(Priority priority) throws Exception {
                if (packageName == null) {
                    return null;
                }
                inputStream = convertPackageNameToIconInputStream(packageName);
                return inputStream;
            }

            @Override
            public void cleanup() {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        inputStream = null;
                    }
                }
            }

            @Override
            public String getId() {
                return packageName;
            }

            @Override
            public void cancel() {

            }
        };
    }

    /**
     * Fetches the icon bitmap of the given package and converts it to a {@link InputStream}
     *
     * @return The result input stream.
     */
    @Nullable
    private InputStream convertPackageNameToIconInputStream(final String packageName) {
        try {
            final Drawable drawable = context.getPackageManager().getApplicationIcon(packageName);
            final Bitmap bitmap = Utils.drawableToBitmap(drawable);

            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);

            return new ByteArrayInputStream(stream.toByteArray());
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e.toString());
        }
        return null;
    }

    static class Factory implements ModelLoaderFactory<String, InputStream> {

        @Override
        public ModelLoader<String, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new AppIconModelLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}
