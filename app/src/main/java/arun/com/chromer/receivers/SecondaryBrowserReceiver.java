package arun.com.chromer.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import arun.com.chromer.util.Preferences;

public class SecondaryBrowserReceiver extends BroadcastReceiver {
    public SecondaryBrowserReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getDataString();

        if (url != null) {
            Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.setComponent(ComponentName.unflattenFromString(
                    Preferences.secondaryBrowserComponent(context)));
            context.startActivity(activityIntent);
            try {
                context.startActivity(activityIntent);
            } catch (Exception e) {
                Toast.makeText(context, "Something went wrong, try again!", Toast.LENGTH_SHORT).show();
                Intent defaultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                Intent chooserIntent = Intent.createChooser(defaultIntent, "Open with..");
                chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chooserIntent);
            }
        } else {
            Toast.makeText(context, "Something went wrong, try again!", Toast.LENGTH_SHORT).show();
        }
    }
}
