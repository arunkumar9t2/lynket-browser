package arun.com.chromer.customtabs.dynamictoolbar;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;

import java.net.URL;

import arun.com.chromer.db.WebColor;
import arun.com.chromer.webheads.helper.RxParser;
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
                    final WebColor webColor = new WebColor(url.getHost(), color);
                    webColor.save();
                } else {
                    Timber.d("Color extraction failed");
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
}
