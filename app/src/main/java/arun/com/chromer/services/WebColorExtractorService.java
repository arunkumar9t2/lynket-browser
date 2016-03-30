package arun.com.chromer.services;

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
        String urlToExtract = intent.getDataString();

        URL url = null;

        int color = 0;
        try {
            url = new URL(urlToExtract);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // attempt to connect
            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String temp;
            String page = "";
            while ((temp = reader.readLine()) != null) {
                page += temp;
                if (temp.contains("</head>")) {
                    break;
                }
            }
            // Test string for checking extraction logic
            // String test = "<meta name=\"theme-color\" content=\"#0041C8\">";

            Matcher matcher = Pattern.compile("<meta name=\"theme-color\"(.*?)>").matcher(page);

            while (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String splitter = "content=\"";
                    String content = matcher.group().split(splitter)[1];
                    color = Color.parseColor(content.split("\">")[0]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Timber.d("Extracted color %d", color);

        if (color != 0) {
            // successful extraction
            Timber.d(url.getHost());
            try {
                // Save this color to DB
                WebColor webColor = new WebColor(url.getHost(), color);
                webColor.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
