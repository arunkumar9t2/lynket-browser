package arun.com.chromer.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.chrometabutilites.CustomActivityHelper;
import arun.com.chromer.chrometabutilites.CustomTabDelegate;

public class WebHeadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String url = getIntent().getData().toString();
        CustomTabsIntent mCustomTabsIntent = CustomTabDelegate.getWebHeadIntent(
                getApplicationContext(),
                url);

        CustomActivityHelper.openCustomTab(this,
                mCustomTabsIntent,
                Uri.parse(getIntent().getData().toString()),
                TabActivity.mCustomTabsFallback);

        finish();
    }
}
