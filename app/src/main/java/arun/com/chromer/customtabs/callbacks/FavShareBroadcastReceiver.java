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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.preferences.manager.Preferences;

import static android.content.Intent.EXTRA_TEXT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.widget.Toast.LENGTH_SHORT;

public class FavShareBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        final String url = intent.getDataString();
        if (url != null) {
            final Intent openAppIntent = new Intent(Intent.ACTION_SEND);
            openAppIntent.putExtra(EXTRA_TEXT, url);
            openAppIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            openAppIntent.setComponent(ComponentName.unflattenFromString(Preferences.favShareComponent(context)));
            try {
                context.startActivity(openAppIntent);
            } catch (Exception e) {
                defaultShare(context, url);
            }
        } else {
            Toast.makeText(context, context.getString(R.string.unsupported_link), LENGTH_SHORT).show();
        }
    }

    private void defaultShare(Context context, String url) {
        Toast.makeText(context, context.getString(R.string.share_failed_msg), LENGTH_SHORT).show();
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(EXTRA_TEXT, url);
        final Intent chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_via));
        chooserIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooserIntent);
    }
}
