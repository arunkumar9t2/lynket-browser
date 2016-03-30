package arun.com.chromer.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.customtabs.CustomActivityHelper;
import arun.com.chromer.customtabs.CustomTabDelegate;
import arun.com.chromer.util.StringConstants;
import arun.com.chromer.util.Util;

public class CustomTabActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String url = getIntent().getData().toString();
        final boolean isWebhead = getIntent().getBooleanExtra(StringConstants.FROM_WEBHEAD, false);

        CustomTabsIntent tabIntent = CustomTabDelegate.getCustomizedTabIntent(getApplicationContext(), url, isWebhead);

        CustomActivityHelper.openCustomTab(this, tabIntent, Uri.parse(url), Util.CUSTOM_TABS_FALLBACK);

        finish();
    }
}
