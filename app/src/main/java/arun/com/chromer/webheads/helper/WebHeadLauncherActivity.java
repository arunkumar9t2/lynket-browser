package arun.com.chromer.webheads.helper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import arun.com.chromer.webheads.WebHeadService;

public class WebHeadLauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            finish();
            return;
        }

        Intent webHeadService = new Intent(this, WebHeadService.class);
        webHeadService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webHeadService.setData(getIntent().getData());
        startService(webHeadService);
        finish();
    }
}
