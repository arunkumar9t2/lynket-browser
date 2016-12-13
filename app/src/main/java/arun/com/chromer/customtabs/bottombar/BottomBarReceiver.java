// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package arun.com.chromer.customtabs.bottombar;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.activities.BrowserInterceptActivity;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.util.Util;
import timber.log.Timber;

public class BottomBarReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final int clickedId = intent.getIntExtra(CustomTabsIntent.EXTRA_REMOTEVIEWS_CLICKED_ID, -1);
        final String url = intent.getDataString();

        if (url == null || clickedId == -1) {
            Timber.d("Skipped bottom bar callback");
            return;
        }

        switch (clickedId) {
            case R.id.bottom_bar_open_in_new_tab:
                new OpenInNewTab(context, url).perform();
                break;
            case R.id.bottom_bar_share:
                new ShareUrl(context, url).perform();
                break;
            case R.id.bottom_bar_minimize_tab:
                new MinimizeUrl(context, url).perform();
                break;
        }
    }

    private static abstract class Command {
        Context mContext;
        final String mUrl;
        boolean performCalled = false;

        Command(@NonNull Context context, @NonNull String url) {
            mContext = context.getApplicationContext();
            mUrl = url;
        }

        void perform() {
            performCalled = true;
            onPerform();
            mContext = null;
        }

        protected abstract void onPerform();
    }

    private static class OpenInNewTab extends Command {

        OpenInNewTab(@NonNull Context context, @NonNull String url) {
            super(context, url);
        }

        @Override
        protected void onPerform() {
            if (!performCalled) {
                throw new IllegalStateException("Should call perform() instead of onPerform()");
            }
            try {
                final ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager.hasPrimaryClip()) {
                    final ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    findAndOpenLink(item.getText().toString());
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""));
                } else
                    invalidLink();
            } catch (Exception e) {
                invalidLink();
                Timber.e(e.getMessage());
            }
        }

        private void findAndOpenLink(@NonNull String text) {
            final List<String> urls = Util.findURLs(text);
            if (urls.size() != 0) {
                // use only the first link
                openLink(urls.get(0));
            } else {
                // No urls were found, so lets do a google search with the text received.
                if (!text.isEmpty()) {
                    text = Constants.G_SEARCH_URL + text.replace(" ", "+");
                    openLink(text);
                } else
                    invalidLink();
            }
        }

        private void openLink(@Nullable String url) {
            if (url == null) {
                invalidLink();
            }
            final Intent websiteIntent = new Intent(mContext, BrowserInterceptActivity.class);
            websiteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            websiteIntent.putExtra(Constants.EXTRA_KEY_FROM_NEW_TAB, true);
            websiteIntent.setData(Uri.parse(url));
            mContext.startActivity(websiteIntent);
        }

        private void invalidLink() {
            Toast.makeText(mContext, mContext.getString(R.string.open_in_new_tab_error), Toast.LENGTH_LONG).show();
        }
    }

    private static class ShareUrl extends Command {

        ShareUrl(@NonNull Context context, @NonNull String url) {
            super(context, url);
        }

        @Override
        protected void onPerform() {
            Util.shareText(mContext, mUrl);
        }
    }

    private static class MinimizeUrl extends Command {

        MinimizeUrl(@NonNull Context context, @NonNull String url) {
            super(context, url);
        }

        @Override
        protected void onPerform() {
            DocumentUtils.minimizeTaskByUrl(mContext, mUrl);
        }
    }
}
