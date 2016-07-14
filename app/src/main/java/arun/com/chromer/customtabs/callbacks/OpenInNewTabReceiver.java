package arun.com.chromer.customtabs.callbacks;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.activities.BrowserInterceptActivity;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.Util;
import timber.log.Timber;

public class OpenInNewTabReceiver extends BroadcastReceiver {
    public OpenInNewTabReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager.hasPrimaryClip()) {
                ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                findAndOpenLink(context, item.getText().toString());
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""));
            }
        } catch (Exception e) {
            invalidLink(context);
            Timber.e(e.getMessage());
        }
    }

    private void findAndOpenLink(Context context, String text) {
        List<String> urls = Util.findURLs(text);
        if (urls.size() != 0) {
            // use only the first link
            String url = urls.get(0);

            openLink(context, url);
        } else {
            // No urls were found, so lets do a google search with the text received.
            if (text.length() != 0) {
                text = Constants.G_SEARCH_URL + text.replace(" ", "+");
                openLink(context, text);
            } else
                invalidLink(context);
        }
    }

    private void openLink(@NonNull Context context, @Nullable String url) {
        if (url == null) {
            invalidLink(context);
        }
        Intent websiteIntent = new Intent(context, BrowserInterceptActivity.class);
        websiteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        websiteIntent.putExtra(Constants.EXTRA_KEY_FROM_NEW_TAB, true);
        websiteIntent.setData(Uri.parse(url));

        context.startActivity(websiteIntent);
    }

    private void invalidLink(@NonNull Context context) {
        Toast.makeText(context, context.getString(R.string.open_in_new_tab_error), Toast.LENGTH_LONG).show();
    }
}
