package arun.com.chromer.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.customtabs.CustomTabBindingHelper;
import arun.com.chromer.customtabs.CustomTabDelegate;
import arun.com.chromer.customtabs.CustomTabHelper;
import arun.com.chromer.util.Constants;
import arun.com.chromer.util.Util;
import timber.log.Timber;

public class CustomTabActivity extends AppCompatActivity {
    private boolean isLoaded = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String url = getIntent().getData().toString();
        final boolean isWebhead = getIntent().getBooleanExtra(Constants.EXTRA_KEY_FROM_WEBHEAD, false);
        final int color = getIntent().getIntExtra(Constants.EXTRA_KEY_WEBHEAD_COLOR, Constants.NO_COLOR);

        CustomTabsIntent tabIntent = CustomTabDelegate.getCustomizedTabIntent(getApplicationContext(), url, isWebhead, color);

        CustomTabBindingHelper.openCustomTab(this, tabIntent, Uri.parse(url), CustomTabHelper.CUSTOM_TABS_FALLBACK);

        setDescription();
        // finish();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setDescription() {
        if (Util.isLollipop()) {
            final Intent intent = getIntent();
            final String title = intent.getStringExtra(Constants.EXTRA_KEY_WEBHEAD_TITLE);
            final Bitmap icon = intent.getParcelableExtra(Constants.EXTRA_KEY_WEBHEAD_ICON);
            setTaskDescription(new ActivityManager.TaskDescription(title, icon));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoaded) {
            Timber.d("Finishing on CTA exit");
            // HACK
            finish();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isLoaded = true;
    }
}
