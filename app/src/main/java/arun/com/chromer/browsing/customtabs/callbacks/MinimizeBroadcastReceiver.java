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

package arun.com.chromer.browsing.customtabs.callbacks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.DocumentUtils;
import timber.log.Timber;

public class MinimizeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String url = intent.getStringExtra(Constants.EXTRA_KEY_ORIGINAL_URL);
        if (url != null) {
            DocumentUtils.minimizeTaskByUrl(context, url);
        } else {
            Timber.e("Error minimizing");
        }
    }
}
