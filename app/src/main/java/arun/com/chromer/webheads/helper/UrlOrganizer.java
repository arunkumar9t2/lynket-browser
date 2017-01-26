package arun.com.chromer.webheads.helper;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.webheads.ui.views.WebHead;
import timber.log.Timber;

import static android.support.customtabs.CustomTabsService.KEY_URL;

/**
 * Created by Arun on 25-01-2017.
 * Organizer class that takes {@link WebHead} list to process it and
 * return a ordered URL list that can be directly commanded to {@link arun.com.chromer.customtabs.CustomTabManager}
 * for pre fetching.
 */
public class UrlOrganizer {
    private Context context;

    public UrlOrganizer(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }


    /**
     * Pre-fetches next set of urls for launch with the given parameter url as reference. The first
     * url in web heads order found is considered as the priority url and other url as likely urls.
     *
     * @param lastOpenedUrl    The last opened url
     * @param customTabManager The manager instance which will help with pre fetching.
     */
    public void prepareNextSetOfUrls(@NonNull Map<String, WebHead> webHeads, @NonNull String lastOpenedUrl, @NonNull CustomTabManager customTabManager) {
        if (Preferences.aggressiveLoading(context)) return;

        final Stack<String> urlStack = getUrlStack(webHeads, lastOpenedUrl);
        if (urlStack.size() > 0) {
            String priorityUrl = urlStack.pop();
            if (priorityUrl == null) return;

            Timber.d("Priority : %s", priorityUrl);

            final List<Bundle> possibleUrls = new ArrayList<>();

            for (String url : urlStack) {
                if (url == null) continue;

                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_URL, Uri.parse(url));
                possibleUrls.add(bundle);
                Timber.d("Others : %s", url);
            }
            // Now let's prepare urls
            boolean ok = customTabManager.mayLaunchUrl(Uri.parse(priorityUrl), null, possibleUrls);
            Timber.d("May launch was %b", ok);
        }
    }

    private Stack<String> getUrlStack(@NonNull Map<String, WebHead> webHeads, @NonNull String lastOpenedUrl) {
        final Stack<String> urlStack = new Stack<>();
        if (webHeads.containsKey(lastOpenedUrl)) {
            boolean foundWebHead = false;
            for (WebHead webhead : webHeads.values()) {
                if (!foundWebHead) {
                    foundWebHead = webhead.getUrl().equalsIgnoreCase(lastOpenedUrl);
                    if (!foundWebHead) urlStack.push(webhead.getUrl());
                } else {
                    urlStack.push(webhead.getUrl());
                }
            }
        } else {
            for (WebHead webhead : webHeads.values()) {
                urlStack.push(webhead.getUrl());
            }
        }
        return urlStack;
    }
}
