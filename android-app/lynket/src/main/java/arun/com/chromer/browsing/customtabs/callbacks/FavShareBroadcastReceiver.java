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

import static android.content.Intent.EXTRA_TEXT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;

public class FavShareBroadcastReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    final String url = intent.getDataString();
    if (url != null) {
      final Intent openAppIntent = new Intent(Intent.ACTION_SEND);
      openAppIntent.putExtra(EXTRA_TEXT, url);
      openAppIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
      openAppIntent.setComponent(ComponentName.unflattenFromString(Preferences.get(context).favShareComponent()));
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
