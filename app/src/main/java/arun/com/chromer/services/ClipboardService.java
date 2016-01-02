package arun.com.chromer.services;

import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import arun.com.chromer.R;

public class ClipboardService extends IntentService {
    public ClipboardService() {
        super("ClipboardService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                final String urlToCopy = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (urlToCopy != null) {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getPackageName(), urlToCopy);
                    clipboard.setPrimaryClip(clip);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(
                                    ClipboardService.this,
                                    getString(R.string.copied) + " " + urlToCopy,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            } else {
                //Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
