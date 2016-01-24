package arun.com.chromer.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;
import timber.log.Timber;

public class ShareInterceptActivity extends AppCompatActivity {


    private static final String TAG = ShareInterceptActivity.class.getSimpleName();

    private static final String SEARCH_URL = "http://www.google.com/search?q=";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
                @SuppressWarnings("ConstantConditions") String text = intent.hasExtra(Intent.EXTRA_TEXT) ?
                        intent.getExtras().getCharSequence(Intent.EXTRA_TEXT).toString() : null;
                Timber.d("Intent. Text: " + text);
                findAndOpenLink(text);
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_PROCESS_TEXT)) {
                final String text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                Timber.d("Process Text Intent. Text: " + text);
                findAndOpenLink(text);
            }
        }
    }

    private void findAndOpenLink(String text) {
        List<String> urls = Util.findURLs(text);
        if (urls != null && urls.size() != 0) {
            // use only the first link
            String url = urls.get(0);

            openInTabActivity(url);
        } else {
            // No urls were found, so lets do a google search with the text received.
            text = SEARCH_URL + text.replace(" ", "+");
            Timber.d(text);
            openInTabActivity(text);
        }
    }

    private void openInTabActivity(String url) {
        if (url == null) {
            invalidLink();
        }
        Timber.d("Opening " + url);
        Intent tabActivity = new Intent(this, TabActivity.class);
        tabActivity.setData(Uri.parse(url));

        startActivity(tabActivity);
        finish();
    }

    private void invalidLink() {
        Toast.makeText(this, getString(R.string.invalid_link), Toast.LENGTH_SHORT).show();
        finish();
    }
}
