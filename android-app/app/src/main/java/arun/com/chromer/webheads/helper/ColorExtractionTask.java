/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.webheads.helper;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import java.lang.ref.WeakReference;

import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.webheads.ui.views.WebHead;
import timber.log.Timber;

/**
 * Created by Arun on 15/05/2016.
 */
public class ColorExtractionTask extends AsyncTask<Void, Void, Integer> {

    private final WeakReference<WebHead> mWebHeadReference;
    private final WeakReference<Bitmap> mFaviconReference;

    public ColorExtractionTask(WebHead webHead, Bitmap favicon) {
        mWebHeadReference = new WeakReference<>(webHead);
        mFaviconReference = new WeakReference<>(favicon);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int bestColor = Constants.NO_COLOR;
        if (mFaviconReference.get() != null) {
            final Bitmap favicon = mFaviconReference.get();
            final Palette palette = Palette.from(favicon)
                    .clearFilters()
                    .generate();
            bestColor = ColorUtil.getBestFaviconColor(palette);
        }
        return bestColor;
    }

    @Override
    protected void onPostExecute(Integer bestColor) {
        try {
            if (bestColor != Constants.NO_COLOR) {
                if (mWebHeadReference.get() != null) {
                    WebHead webHead = mWebHeadReference.get();
                    webHead.setWebHeadColor(bestColor);
                }
            } else Timber.e("Color extraction failed");
            mWebHeadReference.clear();
            mFaviconReference.clear();
        } catch (Exception e) {
            Timber.e(e.toString());
        }
    }

}
