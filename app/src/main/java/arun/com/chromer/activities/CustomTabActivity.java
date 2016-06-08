package arun.com.chromer.activities;

import android.annotation.TargetApi;
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

        CustomTabsIntent tabIntent = CustomTabDelegate.getCustomizedTabIntent(getApplicationContext(), url, isWebhead);

        CustomTabBindingHelper.openCustomTab(this, tabIntent, Uri.parse(url), CustomTabHelper.CUSTOM_TABS_FALLBACK);

        // finish();
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
