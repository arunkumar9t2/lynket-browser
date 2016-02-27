package arun.com.chromer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.db.BlacklistedApps;
import arun.com.chromer.services.AppDetectService;
import arun.com.chromer.util.Preferences;
import timber.log.Timber;

public class BrowserInterceptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if we should blacklist the launching app
        if (Preferences.blacklist(this)) {
            if (AppDetectService.getInstance() != null) {
                String lastApp = AppDetectService.getInstance().getLastApp();
                if (lastApp.length() > 0) {
                    Timber.d("Checking if %s should be blacklisted", lastApp);
                    List<BlacklistedApps> blacklisted = BlacklistedApps.find(BlacklistedApps.class, "package_name = ?", lastApp);
                    if (blacklisted.size() > 0) {
                        // The calling app was found in blacklisted table in DB, so lets show an intent
                        // choose and kill this activity
                        Toast.makeText(this, getString(R.string.blacklist_message), Toast.LENGTH_LONG).show();
                        showIntentChooserAndFinish();
                        return;
                    }
                }
            } else {
                // App detect service was not running. So let's start it.
                startService(new Intent(this, AppDetectService.class));
            }
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
            Intent customTabActivity = new Intent(this, CustomTabActivity.class);
            customTabActivity.setData(getIntent().getData());
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            customTabActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(customTabActivity);
        }

        finish();
    }

    private void showIntentChooserAndFinish() {
        Intent defaultIntent = new Intent(Intent.ACTION_VIEW, getIntent().getData());
        Intent chooserIntent = Intent.createChooser(defaultIntent, "Open with..");
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(chooserIntent);
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
