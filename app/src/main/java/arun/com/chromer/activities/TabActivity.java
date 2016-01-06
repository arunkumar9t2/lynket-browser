package arun.com.chromer.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.chrometabutilites.CustomTabDelegate;
import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;

public class TabActivity extends AppCompatActivity {

    public final static MyCustomActivityHelper.CustomTabsFallback mCustomTabsFallback =
            new MyCustomActivityHelper.CustomTabsFallback() {
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
    private static final String TAG = TabActivity.class.getSimpleName();

    private MyCustomActivityHelper mCustomTabActivityHelper;

    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), Toast.LENGTH_SHORT).show();
            finish();
            // TODO handle no intent later
            return;
        }

        final String url = getIntent().getData().toString();
        CustomTabsIntent mCustomTabsIntent = CustomTabDelegate.getCustomizedTabIntent(
                getApplicationContext(), url);

        mCustomTabActivityHelper = new MyCustomActivityHelper();
        mCustomTabActivityHelper.setConnectionCallback(
                new MyCustomActivityHelper.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                        Log.d(TAG, "Connect to custom tab");
                        try {
                            Log.d(TAG, "Gave may launch command");
                            mCustomTabActivityHelper.mayLaunchUrl(
                                    Uri.parse(url)
                                    , null, null);
                        } catch (Exception e) {
                            // Don't care. Yes.. You heard me.
                        }
                    }

                    @Override
                    public void onCustomTabsDisconnected() {
                        finish();
                    }
                });

        MyCustomActivityHelper.openCustomTab(this, mCustomTabsIntent,
                Uri.parse(getIntent().getData().toString()),
                mCustomTabsFallback);
        finish();
    }
}
