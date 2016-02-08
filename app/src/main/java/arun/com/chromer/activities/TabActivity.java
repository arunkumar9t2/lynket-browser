package arun.com.chromer.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.chrometabutilites.CustomActivityHelper;
import arun.com.chromer.chrometabutilites.CustomTabDelegate;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.webheads.WebHeadService;

public class TabActivity extends AppCompatActivity {

    public final static CustomActivityHelper.CustomTabsFallback mCustomTabsFallback =
            new CustomActivityHelper.CustomTabsFallback() {
                @Override
                public void openUri(Activity activity, Uri uri) {

                    if (activity != null) {
                        Toast.makeText(activity,
                                activity.getString(R.string.fallback_msg),
                                Toast.LENGTH_SHORT).show();
                        try {
                            activity.startActivity(Intent.createChooser(
                                    new Intent(Intent.ACTION_VIEW, uri),
                                    activity.getString(R.string.open_with)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity,
                                    activity.getString(R.string.unxp_err), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            };

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
            final String url = getIntent().getData().toString();
            CustomTabsIntent mCustomTabsIntent = CustomTabDelegate.getCustomizedTabIntent(
                    getApplicationContext(),
                    url);

            CustomActivityHelper.openCustomTab(this,
                    mCustomTabsIntent,
                    Uri.parse(getIntent().getData().toString()),
                    mCustomTabsFallback);
        }

        finish();
    }

    private void launchWebHead() {
        Intent webHeadService = new Intent(this, WebHeadService.class);
        webHeadService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webHeadService.setData(getIntent().getData());
        startService(webHeadService);
    }

}
