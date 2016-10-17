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

package arun.com.chromer.customtabs.callbacks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import arun.com.chromer.shared.Constants;
import arun.com.chromer.webheads.WebHeadService;
import timber.log.Timber;

/**
 * A BroadcastReceiver that handles the Action Intent from the Custom Tab and fires a Share Intent.
 */
public class MinimizeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (url != null) {
            sendMinimizeIntent(context, url);
            signalWebHead(context, url);
        } else {
            Timber.e("Error");
        }
    }

    private void signalWebHead(Context context, @NonNull String url) {
        final Intent webHeadService = new Intent(context, WebHeadService.class);
        webHeadService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webHeadService.setData(Uri.parse(url));
        webHeadService.putExtra(Constants.EXTRA_KEY_MINIMIZE, true);
        context.startService(webHeadService);
    }

    private void sendMinimizeIntent(Context context, String url) {
        final Intent minimizeIntent = new Intent(Constants.ACTION_MINIMIZE);
        minimizeIntent.putExtra(Intent.EXTRA_TEXT, url);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(minimizeIntent);
    }
}
