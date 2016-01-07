package arun.com.chromer.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;

import arun.com.chromer.model.WebColor;
import arun.com.chromer.util.ToolbarColorUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class ColorExtractor extends IntentService {

    public ColorExtractor() {
        super(ColorExtractor.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getDataString();

        int color = -1;
        try {
            // Attempting to extract colors
            OkHttpClient httpClient = new OkHttpClient();

            Response response = httpClient.newCall(new Request.Builder()
                    .url(url)
                    .build())
                    .execute();

            BufferedReader reader = new BufferedReader(response.body().charStream());

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.contains("<meta name=\"theme-color\" content=\"")) {
                    String substring = line.substring(line.indexOf('#'), line.lastIndexOf('"'));
                    color = Color.parseColor(substring);
                    break;
                }

                if (line.contains("</head>")) {
                    break;
                }
            }
            reader.close();
            response.body().close();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();

        }
        Timber.d("Extracted color =  " + color + " for" + url);

        if (color != -1) {
            WebColor webColor = new WebColor();
            webColor.host = Uri.parse(url).getHost();
            webColor.toolbarColor = color;

            ToolbarColorUtil.insertColor(webColor);
        }

    }
}
