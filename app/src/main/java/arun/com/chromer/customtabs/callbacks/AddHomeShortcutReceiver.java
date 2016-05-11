package arun.com.chromer.customtabs.callbacks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.activities.BrowserInterceptActivity;
import arun.com.chromer.util.Constants;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;

public class AddHomeShortcutReceiver extends BroadcastReceiver {

    public AddHomeShortcutReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String urlToAdd = intent.getDataString();
            if (urlToAdd != null) {
                Timber.d("Attempting to add for %s", urlToAdd);

                Intent webIntent = new Intent(context, BrowserInterceptActivity.class);
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                webIntent.setData(Uri.parse(urlToAdd));

                String hostName = Uri.parse(urlToAdd).getHost();
                String shortcutName = hostName == null ? urlToAdd : hostName;

                Intent addIntent = new Intent(Constants.ACTION_INSTALL_SHORTCUT);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, webIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher));

                context.sendBroadcast(addIntent);

                Toast.makeText(context, context.getString(R.string.added) + " " + shortcutName, LENGTH_SHORT).show();
            }
        }

    }
}
