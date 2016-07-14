package arun.com.chromer.activities;

import android.annotation.SuppressLint;
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
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.Util;
import timber.log.Timber;

@SuppressLint("GoogleAppIndexingApiWarning")
public class ShareInterceptActivity extends AppCompatActivity {


    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        try {
            if (intent != null) {
                final String action = intent.getAction();
                String text = null;
                switch (action) {
                    case Intent.ACTION_SEND:
                        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                            text = intent.getExtras().getCharSequence(Intent.EXTRA_TEXT).toString();
                        }
                        break;
                    case Intent.ACTION_PROCESS_TEXT:
                        text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                        break;
                }
                findAndOpenLink(text);
            }
        } catch (Exception exception) {
            invalidLink();
            Timber.e(exception.getMessage());
        } finally {
            finish();
        }
    }


    private void findAndOpenLink(@Nullable String text) {
        if (text == null) return;

        final List<String> urls = Util.findURLs(text);
        if (!urls.isEmpty()) {
            // use only the first link
            final String url = urls.get(0);
            // TODO launch group of web heads if web heads enabled
            openLink(url);
        } else {
            // No urls were found, so lets do a google search with the text received.
            text = Constants.G_SEARCH_URL + text.replace(" ", "+");
            openLink(text);
        }
    }

    private void openLink(@Nullable String url) {
        if (url == null) {
            invalidLink();
        }
        final Intent websiteIntent = new Intent(this, BrowserInterceptActivity.class);
        websiteIntent.setData(Uri.parse(url));
        startActivity(websiteIntent);
        finish();
    }

    private void invalidLink() {
        Toast.makeText(this, getString(R.string.invalid_link), Toast.LENGTH_SHORT).show();
        finish();
    }
}
