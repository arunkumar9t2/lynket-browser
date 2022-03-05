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

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import arun.com.chromer.settings.Preferences;
import arun.com.chromer.util.Utils;

public class OpenInChromeReceiver extends BroadcastReceiver {
  public OpenInChromeReceiver() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    final String url = intent.getDataString();
    if (url != null) {
      final String customTabPkg = Preferences.get(context).customTabPackage();
      if (Utils.isPackageInstalled(context, customTabPkg)) {
        final Intent chromeIntentExplicit = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        final ComponentName cN = Utils.getBrowserComponentForPackage(context, customTabPkg);
        if (cN != null) {
          chromeIntentExplicit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          chromeIntentExplicit.setComponent(cN);
          try {
            context.startActivity(chromeIntentExplicit);
          } catch (ActivityNotFoundException ignored) {

          }
        }
      }
    }
  }
}
