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

import arun.com.chromer.util.DocumentUtils;
import timber.log.Timber;

/**
 * A BroadcastReceiver that handles the Action Intent from the Custom Tab and fires a Share Intent.
 */
public class MinimizeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (url != null) {
            DocumentUtils.minimizeTaskByUrl(context, url);
        } else {
            Timber.e("Error minimizing");
        }
    }
}
