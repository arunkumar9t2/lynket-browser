package arun.com.chromer.customtabs.callbacks;

import android.annotation.SuppressLint;
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
import arun.com.chromer.activities.settings.preferences.manager.Preferences;
import timber.log.Timber;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SecondaryBrowserReceiver extends BroadcastReceiver {
    public SecondaryBrowserReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String url = intent.getDataString();
        if (url != null) {
            final Intent webIntentExplicit = new Intent(ACTION_VIEW, Uri.parse(url));
            webIntentExplicit.setFlags(FLAG_ACTIVITY_NEW_TASK);
            final String componentFlatten = Preferences.secondaryBrowserComponent(context);
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
        final Intent webIntentImplicit = new Intent(ACTION_VIEW, Uri.parse(url));
        webIntentImplicit.setFlags(FLAG_ACTIVITY_NEW_TASK);
        @SuppressLint("InlinedApi")
        final List<ResolveInfo> resolvedActivityList = context.getPackageManager().queryIntentActivities(webIntentImplicit, PackageManager.MATCH_ALL);
        final String secondaryPackage = Preferences.secondaryBrowserPackage(context);
        if (secondaryPackage != null) {
            boolean found = false;
            for (ResolveInfo info : resolvedActivityList) {
                if (info.activityInfo.packageName.equalsIgnoreCase(secondaryPackage)) {
                    found = true;
                    final ComponentName componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                    webIntentImplicit.setComponent(componentName);
                    // This will be the new component, so write it to preferences
                    Preferences.secondaryBrowserComponent(context, componentName.flattenToString());
                    context.startActivity(webIntentImplicit);
                    break;
                }
            }
            if (!found) showChooser(context, url);
        }
    }

    private void showChooser(Context context, String url) {
        Timber.d("Falling back to intent chooser");
        Toast.makeText(context, context.getString(R.string.unxp_err), Toast.LENGTH_SHORT).show();
        final Intent implicitViewIntent = new Intent(ACTION_VIEW, Uri.parse(url));
        Intent chooserIntent = Intent.createChooser(implicitViewIntent, context.getString(R.string.open_with));
        chooserIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooserIntent);
    }
}
