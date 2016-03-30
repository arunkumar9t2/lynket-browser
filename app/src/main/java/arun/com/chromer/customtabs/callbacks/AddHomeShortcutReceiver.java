package arun.com.chromer.customtabs.callbacks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.activities.BrowserInterceptActivity;
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

                Intent openTabIntent = new Intent(context, BrowserInterceptActivity.class);
                openTabIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                openTabIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                openTabIntent.setData(Uri.parse(urlToAdd));

                String shortcutName = Uri.parse(urlToAdd).getHost() == null
                        ? urlToAdd : Uri.parse(urlToAdd).getHost();

                Intent addIntent = new Intent();
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, openTabIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher));
                addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

                context.sendBroadcast(addIntent);

                Toast.makeText(context, context.getString(R.string.added) + " " + shortcutName,
                        LENGTH_SHORT).show();
            }
        }

    }
}
