package arun.com.chromer.customtabs.callbacks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.DocumentUtils;
import timber.log.Timber;

public class MinimizeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String url = intent.getStringExtra(Constants.EXTRA_KEY_ORIGINAL_URL);
        if (url != null) {
            DocumentUtils.minimizeTaskByUrl(context, url);
        } else {
            Timber.e("Error minimizing");
        }
    }
}
