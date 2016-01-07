package arun.com.chromer.chrometabutilites;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.util.PrefUtil;
import timber.log.Timber;

public class SecondaryBrowserReceiver extends BroadcastReceiver {
    public SecondaryBrowserReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Came to secondary browser reciever!");
        String url = intent.getDataString();

        if (url != null) {
            String secondaryPackage = PrefUtil.getSecondaryPref(context);

            Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            List<ResolveInfo> resolvedActivityList = context.getPackageManager()
                    .queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
            for (ResolveInfo info : resolvedActivityList) {
                if (info.activityInfo.packageName.equalsIgnoreCase(secondaryPackage))
                    activityIntent.setComponent(new ComponentName(info.activityInfo.packageName,
                            info.activityInfo.name));
            }
            context.startActivity(activityIntent);
        } else {
            Toast.makeText(context, "Something went wrong, try again!", Toast.LENGTH_SHORT).show();
        }
    }
}
