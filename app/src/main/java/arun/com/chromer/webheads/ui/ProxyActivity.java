package arun.com.chromer.webheads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import arun.com.chromer.shared.Constants;
import arun.com.chromer.webheads.WebHeadService;
import timber.log.Timber;

public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Cleared");

        if (getIntent() == null || getIntent().getData() == null) {
            finish();
            return;
        }
        boolean isFromNewTab = getIntent().getBooleanExtra(Constants.EXTRA_KEY_FROM_NEW_TAB, false);
        final Intent webHeadService = new Intent(this, WebHeadService.class);
        webHeadService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webHeadService.setData(getIntent().getData());
        webHeadService.putExtra(Constants.EXTRA_KEY_FROM_NEW_TAB, isFromNewTab);
        startService(webHeadService);
        finish();
    }
}
