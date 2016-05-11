package arun.com.chromer.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.util.Constants;
import arun.com.chromer.util.Util;

public class ShareInterceptActivity extends AppCompatActivity {


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        try {
            if (intent != null) {
                if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
                    @SuppressWarnings("ConstantConditions")
                    String text = intent.hasExtra(Intent.EXTRA_TEXT) ?
                            intent.getExtras().getCharSequence(Intent.EXTRA_TEXT).toString() : null;
                    findAndOpenLink(text);
                } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_PROCESS_TEXT)) {
                    final String text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                    findAndOpenLink(text);
                }
            }
        } catch (NullPointerException npe) {
            invalidLink();
        }
    }

    private void findAndOpenLink(String text) {
        List<String> urls = Util.findURLs(text);
        if (urls != null && urls.size() != 0) {
            // use only the first link
            String url = urls.get(0);

            openLink(url);
        } else {
            // No urls were found, so lets do a google search with the text received.
            text = Constants.SEARCH_URL + text.replace(" ", "+");
            openLink(text);
        }
    }

    private void openLink(@Nullable String url) {
        if (url == null) {
            invalidLink();
        }

        Intent websiteIntent = new Intent(this, BrowserInterceptActivity.class);
        websiteIntent.setData(Uri.parse(url));

        startActivity(websiteIntent);
        finish();
    }

    private void invalidLink() {
        Toast.makeText(this, getString(R.string.invalid_link), Toast.LENGTH_SHORT).show();
        finish();
    }
}
