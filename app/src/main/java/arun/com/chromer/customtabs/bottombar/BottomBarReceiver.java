/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.customtabs.bottombar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;

import arun.com.chromer.R;
import arun.com.chromer.activities.NewTabDialogActivity;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.util.Utils;
import timber.log.Timber;

public class BottomBarReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final int clickedId = intent.getIntExtra(CustomTabsIntent.EXTRA_REMOTEVIEWS_CLICKED_ID, -1);
        final String url = intent.getDataString();
        final String orgUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
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
                if (orgUrl != null)
                    new MinimizeUrl(context, orgUrl).perform();
                break;
        }
    }

    private static abstract class Command {
        Context context;
        final String url;
        boolean performCalled = false;

        Command(@NonNull Context context, @NonNull String url) {
            this.context = context.getApplicationContext();
            this.url = url;
        }

        void perform() {
            performCalled = true;
            onPerform();
            context = null;
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
            final Intent newTabIntent = new Intent(context, NewTabDialogActivity.class);
            newTabIntent.setData(Uri.parse(url));
            newTabIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newTabIntent);
        }

    }

    private static class ShareUrl extends Command {

        ShareUrl(@NonNull Context context, @NonNull String url) {
            super(context, url);
        }

        @Override
        protected void onPerform() {
            Utils.shareText(context, url);
        }
    }

    private static class MinimizeUrl extends Command {

        MinimizeUrl(@NonNull Context context, @NonNull String url) {
            super(context, url);
        }

        @Override
        protected void onPerform() {
            DocumentUtils.minimizeTaskByUrl(context, url);
        }
    }
}
