/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.browsing.customtabs.callbacks;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import arun.com.chromer.R;

public class CopyToClipboardReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    final String urlToCopy = intent.getDataString();
    if (urlToCopy != null) {
      ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip = ClipData.newPlainText(context.getPackageName(), urlToCopy);
      clipboard.setPrimaryClip(clip);
      showToast(context, context.getString(R.string.copied) + " " + urlToCopy);
    } else {
      showToast(context, context.getString(R.string.unxp_err));
    }
  }

  private void showToast(Context context, final String msgToShow) {
    Toast.makeText(context, msgToShow, Toast.LENGTH_SHORT).show();
  }
}
