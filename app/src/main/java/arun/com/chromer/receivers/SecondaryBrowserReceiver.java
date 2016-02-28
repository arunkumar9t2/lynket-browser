package arun.com.chromer.receivers;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.util.Preferences;
import timber.log.Timber;

public class SecondaryBrowserReceiver extends BroadcastReceiver {
    public SecondaryBrowserReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getDataString();

        if (url != null) {
            Intent webIntentExplicit = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            webIntentExplicit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            String componentFlatten = Preferences.secondaryBrowserComponent(context);

            if (componentFlatten != null) {
                ComponentName cN = ComponentName.unflattenFromString(componentFlatten);
                webIntentExplicit.setComponent(cN);

                try {
                    context.startActivity(webIntentExplicit);
                } catch (ActivityNotFoundException e) {
                    launchComponentWithIteration(context, url);
                }
            } else showChooser(context, url);
        } else {
            Toast.makeText(context, context.getString(R.string.unsupported_link), Toast.LENGTH_LONG).show();
        }
    }

    private void launchComponentWithIteration(Context context, String url) {
        Timber.d("Attempting to launch activity with iteration");

        Intent webIntentImplicit = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        webIntentImplicit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        List<ResolveInfo> resolvedActivityList = context.getPackageManager()
                .queryIntentActivities(webIntentImplicit, PackageManager.MATCH_ALL);

        String secondaryPackage = Preferences.secondaryBrowserPackage(context);

        if (secondaryPackage != null) {
            boolean found = false;
            for (ResolveInfo info : resolvedActivityList) {
                if (info.activityInfo.packageName.equalsIgnoreCase(secondaryPackage)) {
                    found = true;

                    ComponentName componentName = new ComponentName(info.activityInfo.packageName,
                            info.activityInfo.name);
                    webIntentImplicit.setComponent(componentName);

                    // This will be the new component, so write it to preferences
                    Preferences.secondaryBrowserComponent(context, componentName.flattenToString());

                    context.startActivity(webIntentImplicit);
                }
            }
            if (!found) showChooser(context, url);
        }
    }

    private void showChooser(Context context, String url) {
        Timber.d("Falling back to intent chooser");

        Toast.makeText(context, context.getString(R.string.unxp_err), Toast.LENGTH_SHORT).show();
        Intent implicitViewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        Intent chooserIntent = Intent.createChooser(implicitViewIntent, context.getString(R.string.open_with));
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooserIntent);
    }
}
