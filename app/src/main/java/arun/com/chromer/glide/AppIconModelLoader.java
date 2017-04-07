/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import arun.com.chromer.data.common.App;
import arun.com.chromer.util.Utils;
import timber.log.Timber;

class AppIconModelLoader implements StreamModelLoader<App> {
    private final Context context;

    private AppIconModelLoader(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(final App app, int width, int height) {
        return new DataFetcher<InputStream>() {
            InputStream inputStream;

            @Override
            public InputStream loadData(Priority priority) throws Exception {
                if (app == null) {
                    return null;
                }
                inputStream = convertPackageNameToIconInputStream(app.packageName);
                return inputStream;
            }

            @Override
            public void cleanup() {
                closeStream();
            }

            private void closeStream() {
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
                return app.packageName;
            }

            @Override
            public void cancel() {
                closeStream();
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
            Drawable drawable = context.getPackageManager().getApplicationIcon(packageName);
            Bitmap bitmap = Utils.drawableToBitmap(drawable);

            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);

            final ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
            // bitmap.recycle();
            bitmap = null;
            drawable = null;
            return inputStream;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e.toString());
        }
        return null;
    }

    static class Factory implements ModelLoaderFactory<App, InputStream> {

        @Override
        public ModelLoader<App, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new AppIconModelLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}
