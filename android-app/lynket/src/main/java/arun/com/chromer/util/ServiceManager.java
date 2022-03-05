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

package arun.com.chromer.util;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import arun.com.chromer.appdetect.AppDetectService;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.shared.Constants;

/**
 * Created by Arun on 30/01/2016.
 */
public class ServiceManager {
  private ServiceManager() {
    throw new AssertionError("Cannot instantiate");
  }

  public static void takeCareOfServices(@NonNull Context context) {
    if (shouldRunAppDetection(context)) {
      startAppDetectionService(context);
    } else {
      stopAppDetectionService(context);
    }
  }

  private static boolean shouldRunAppDetection(@NonNull Context context) {
    return Preferences.get(context).isAppBasedToolbar() || Preferences.get(context).perAppSettings();
  }

  public static void startAppDetectionService(@NonNull Context context) {
    ContextCompat.startForegroundService(context, new Intent(context, AppDetectService.class)
      .putExtra(Constants.EXTRA_KEY_CLEAR_LAST_TOP_APP, true));
  }

  public static void stopAppDetectionService(@NonNull Context context) {
    context.stopService(new Intent(context, AppDetectService.class));
  }

  public static void refreshCustomTabBindings(@NonNull Context context) {
    final Intent intent = new Intent(Constants.ACTION_REBIND_WEBHEAD_TAB_CONNECTION);
    intent.putExtra(Constants.EXTRA_KEY_REBIND_WEBHEAD_CXN, true);
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }

  public static void restartAppDetectionService(Context context) {
    if (shouldRunAppDetection(context)) {
      stopAppDetectionService(context);
      startAppDetectionService(context);
    }
  }
}
