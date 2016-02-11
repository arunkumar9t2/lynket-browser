package arun.com.chromer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.util.Preferences;

public class BrowserInterceptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // If user prefers to open in bubbles, then start the web head service which will take care
        // of pre fetching and loading the bubble. We don't need this activity anymore, so we will
        // finish this silently.
        if (Preferences.webHeads(this)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, getString(R.string.web_head_permission_toast), Toast.LENGTH_LONG).show();
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    launchWebHead();
                }
            } else {
                launchWebHead();
            }
        } else {
            Intent webHeadActivity = new Intent(this, CustomTabActivity.class);
            webHeadActivity.setData(getIntent().getData());
            webHeadActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            webHeadActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(webHeadActivity);
        }

        finish();
    }

    private void launchWebHead() {
        Intent webHeadLauncher = new Intent(this, WebHeadLauncherActivity.class);
        webHeadLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webHeadLauncher.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        webHeadLauncher.setData(getIntent().getData());
        startActivity(webHeadLauncher);
    }
}
