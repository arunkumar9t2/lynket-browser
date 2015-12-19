package arun.com.chromer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class TabActivity extends AppCompatActivity {
    private final MyCustomActivityHelper.CustomTabsFallback mCustomTabsFallback =
            new MyCustomActivityHelper.CustomTabsFallback() {
                @Override
                public void openUri(Activity activity, Uri uri) {
                    Toast.makeText(TabActivity.this,
                            "Could not open custom tab, fall back to browsers"
                            , Toast.LENGTH_SHORT).show();
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity, "No custom tab compatible browsers found", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, "Not a supported link, try again", Toast.LENGTH_SHORT).show();
            finish();
            // TODO handle no intent later
            return;
        }

        CustomTabHelperFragMine mCustomTabHelperFragMine = CustomTabHelperFragMine.attachTo(this);
        CustomTabsIntent mCustomTabsIntent = Util.getCutsomizedTabIntent(this);

        mCustomTabHelperFragMine.setConnectionCallback(
                new MyCustomActivityHelper.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                    }

                    @Override
                    public void onCustomTabsDisconnected() {
                        finish();
                    }
                });

        CustomTabHelperFragMine.open(this, mCustomTabsIntent,
                Uri.parse(getIntent().getData().toString()),
                mCustomTabsFallback);
        finish();
    }
}
