package arun.com.chromer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class ShareInterceptActivity extends AppCompatActivity {


    private static final String TAG = ShareInterceptActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
                String text = intent.hasExtra(Intent.EXTRA_TEXT) ?
                        intent.getExtras().getCharSequence(Intent.EXTRA_TEXT).toString() : null;
                Log.d(TAG, "Intent. Text: " + text);
                findAndOpenLink(text);
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_PROCESS_TEXT)) {
                final String text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                Log.d(TAG, "Process Text Intent. Text: " + text);
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
            invalidLink();
        }
    }

    private void openInTabActivity(String url) {
        if (url == null) {
            invalidLink();
        }
        Log.d(TAG, "Opening " + url);
        Intent tabActivity = new Intent(this, TabActivity.class);
        tabActivity.setData(Uri.parse(url));

        startActivity(tabActivity);
        finish();
    }

    private void invalidLink() {
        Toast.makeText(this, "Invalid link", Toast.LENGTH_SHORT).show();
        finish();
    }
}
