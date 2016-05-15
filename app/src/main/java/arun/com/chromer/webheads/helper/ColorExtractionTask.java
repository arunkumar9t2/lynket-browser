package arun.com.chromer.webheads.helper;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arun.com.chromer.util.Util;
import arun.com.chromer.webheads.ui.WebHead;
import timber.log.Timber;

/**
 * Created by Arun on 15/05/2016.
 */
public class ColorExtractionTask extends AsyncTask<Void, Void, Integer> {
    private static final int NO_COLOR = -1;

    private final WeakReference<WebHead> mWebHeadReference;
    private final WeakReference<Bitmap> mFaviconReference;

    public ColorExtractionTask(WebHead webHead, Bitmap favicon) {
        mWebHeadReference = new WeakReference<>(webHead);
        mFaviconReference = new WeakReference<>(favicon);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int bestColor = NO_COLOR;
        if (mFaviconReference.get() != null) {
            Bitmap favicon = mFaviconReference.get();
            Palette palette = Palette.from(favicon).generate();

            bestColor = getBestColor(palette);
        }

        return bestColor;
    }

    @Override
    protected void onPostExecute(Integer bestColor) {
        if (bestColor != NO_COLOR) {
            Timber.d("Got %d for favicon", bestColor);
            if (mWebHeadReference.get() != null) {
                WebHead webHead = mWebHeadReference.get();
                webHead.setWebHeadColor(bestColor);
            }
        } else Timber.e("Color extraction failed");
    }

    @ColorInt
    private int getBestColor(@Nullable Palette palette) {
        if (palette != null) {
            List<Palette.Swatch> sortedSwatch = Util.getSwatchList(palette);
            // Descending
            Collections.sort(sortedSwatch,
                    new Comparator<Palette.Swatch>() {
                        @Override
                        public int compare(Palette.Swatch swatch1, Palette.Swatch swatch2) {
                            int a = swatch1 == null ? 0 : swatch1.getPopulation();
                            int b = swatch2 == null ? 0 : swatch2.getPopulation();
                            return b - a;
                        }
                    });

            // We want the vibrant color but we will avoid it if it is the most prominent one.
            // Instead we will choose the next prominent color
            int vibrantColor = palette.getVibrantColor(NO_COLOR);
            int prominentColor = sortedSwatch.get(0).getRgb();
            if (vibrantColor == NO_COLOR || vibrantColor == prominentColor) {
                int darkVibrantColor = palette.getDarkVibrantColor(NO_COLOR);
                if (darkVibrantColor != NO_COLOR) {
                    return darkVibrantColor;
                } else {
                    int mutedColor = palette.getMutedColor(NO_COLOR);
                    if (mutedColor != NO_COLOR) {
                        return mutedColor;
                    } else {
                        int lightVibrantColor = palette.getLightVibrantColor(NO_COLOR);
                        if (lightVibrantColor != NO_COLOR) {
                            return lightVibrantColor;
                        } else return prominentColor;
                    }
                }
            } else return vibrantColor;
        }
        return NO_COLOR;
    }
}
