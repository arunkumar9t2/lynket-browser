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

package arun.com.chromer.customtabs.dynamictoolbar;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;

import java.net.URL;

import arun.com.chromer.parser.RxParser;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * Created by Arun on 06/01/2016.
 */
public class WebColorExtractorService extends IntentService {

    public WebColorExtractorService() {
        super(WebColorExtractorService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getDataString() != null) {
            final String urlToExtract = intent.getDataString();
            URL url;
            int color = NO_COLOR;
            try {
                url = new URL(urlToExtract);
                color = Color.parseColor(RxParser.parseUrlSync(urlToExtract).themeColor);
                if (color != NO_COLOR) {
                    Timber.d("Extracted color %d for %s", color, url.getHost());
                    /*final WebColor webColor = new WebColor(url.getHost(), color);
                    webColor.save();*/
                } else {
                    Timber.d("Color extraction failed");
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
}
