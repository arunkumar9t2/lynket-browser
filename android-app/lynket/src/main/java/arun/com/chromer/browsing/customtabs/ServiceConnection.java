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

package arun.com.chromer.browsing.customtabs;

import android.content.ComponentName;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;

import java.lang.ref.WeakReference;

/**
 * Implementation for the CustomTabsServiceConnection that avoids leaking the
 * ServiceConnectionCallback
 */
@SuppressWarnings("WeakerAccess")
public class ServiceConnection extends CustomTabsServiceConnection {
  // A weak reference to the ServiceConnectionCallback to avoid leaking it.
  @SuppressWarnings("CanBeFinal")
  private WeakReference<ServiceConnectionCallback> mConnectionCallback;

  public ServiceConnection(ServiceConnectionCallback connectionCallback) {
    mConnectionCallback = new WeakReference<>(connectionCallback);
  }

  @Override
  public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
    ServiceConnectionCallback connectionCallback = mConnectionCallback.get();
    if (connectionCallback != null) connectionCallback.onServiceConnected(client);
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    ServiceConnectionCallback connectionCallback = mConnectionCallback.get();
    if (connectionCallback != null) connectionCallback.onServiceDisconnected();
  }
}
