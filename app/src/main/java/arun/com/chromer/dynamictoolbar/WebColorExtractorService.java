package arun.com.chromer.dynamictoolbar;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.db.WebColor;
import arun.com.chromer.shared.Constants;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class WebColorExtractorService extends IntentService {

    public WebColorExtractorService() {
        super(WebColorExtractorService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String urlToExtract = intent.getDataString();
        URL url = null;
        int color = Constants.NO_COLOR;
        try {
            url = new URL(urlToExtract);

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // attempt to connect
            final InputStream inputStream = connection.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String temp;
            String page = "";
            while ((temp = reader.readLine()) != null) {
                page += temp;
                if (temp.contains("</head>")) {
                    break;
                }
            }
            final Matcher matcher = Pattern.compile("<meta name=\"theme-color\"(.*?)>").matcher(page);
            while (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    final String splitter = "content=\"";
                    final String content = matcher.group().split(splitter)[1];
                    color = Color.parseColor(content.split("\">")[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (color != Constants.NO_COLOR) {
            // successful extraction
            try {
                Timber.d("Extracted color %d for %s", color, url.getHost());

                final WebColor webColor = new WebColor(url.getHost(), color);
                webColor.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Timber.d("Color extraction failed");
        }
    }
}
