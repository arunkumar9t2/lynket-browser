package arun.com.chromer.chrometabutilites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.TabActivity;

import static android.widget.Toast.LENGTH_SHORT;

public class AddHomeShortcutReceiver extends BroadcastReceiver {
    private static final String TAG = AddHomeShortcutReceiver.class.getSimpleName();

    public AddHomeShortcutReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                final String urlToCopy = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (urlToCopy != null) {
                    Log.d(TAG, "Attempting to add for " + urlToCopy);
                    Intent openUrl = new Intent(context, TabActivity.class);
                    // TODO fix return behaviour
                    openUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    openUrl.setData(Uri.parse(urlToCopy));

                    String shortcutName = Uri.parse(urlToCopy).getHost() == null
                            ? urlToCopy : Uri.parse(urlToCopy).getHost();
                    Intent addIntent = new Intent();
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, openUrl);
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(context,
                                    R.mipmap.ic_launcher));
                    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                    context.sendBroadcast(addIntent);
                    Toast.makeText(context, "Added " + shortcutName, LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
