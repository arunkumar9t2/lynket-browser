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

import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import arun.com.chromer.R;

// TODO Rewrite this using broadcast receiver
public class CopyToClipboardService extends IntentService {
    public CopyToClipboardService() {
        super("CopyToClipboardService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String urlToCopy = intent.getDataString();
        if (urlToCopy != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getPackageName(), urlToCopy);
            clipboard.setPrimaryClip(clip);
            showToast(getString(R.string.copied) + " " + urlToCopy);
        } else {
            showToast(getString(R.string.unxp_err));
        }
    }

    private void showToast(final String msgToShow) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(CopyToClipboardService.this, msgToShow, Toast.LENGTH_SHORT).show());
    }
}
