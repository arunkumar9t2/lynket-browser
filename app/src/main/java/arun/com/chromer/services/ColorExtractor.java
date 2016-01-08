package arun.com.chromer.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.util.ChromerDatabaseUtil;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class ColorExtractor extends IntentService {

    private static String splitter = "content=\"";

    public ColorExtractor() {
        super(ColorExtractor.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url1 = intent.getDataString();

        // Testing link
        URL urll = null;

        int color = 0;
        try {
            urll = new URL(url1);

            HttpURLConnection connection = (HttpURLConnection) urll.openConnection();
            // attempt to connect
            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String temp = "";
            String page = "";
            while ((temp = reader.readLine()) != null) {
                page += temp;
                if (temp.contains("</head>")) {
                    Timber.d("Stopping");
                    break;
                }
            }

            String test = "<meta name=\"theme-color\" content=\"#0041C8\">";

            Matcher matcher = Pattern.compile("<meta name=\\\"theme-color\\\"(.*?)>").matcher(page);

            while (matcher.find()) {
                Timber.d("Found" + matcher.groupCount());
                for (int i = 0; i < matcher.groupCount(); i++) {
                    Timber.d(matcher.group(i));
                    String content = matcher.group().split(splitter)[1];
                    color = Color.parseColor(content.split("\">")[0]);
                }
            }

            // Attempt to extract the color tag using regex.
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Timber.d("Extracted color " + color);

        if (color != 0) {
            // successful extraction
            Timber.d(urll.getHost());
            // Attempt to install
            try {
                new ChromerDatabaseUtil(this).insertColor(color, urll.getHost());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
