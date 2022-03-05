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

package arun.com.chromer.payments.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for the "com.android.vending.billing.PURCHASES_UPDATED" Action
 * from the Play Store.
 * <p/>
 * <p>It is possible that an in-app item may be acquired without the
 * application calling getBuyIntent(), for example if the item can be
 * redeemed from inside the Play Store using a promotional code. If this
 * application isn't running at the time, then when it is started a call
 * to getPurchases() will be sufficient notification. However, if the
 * application is already running in the background when the item is acquired,
 * a message to this BroadcastReceiver will indicate that the an item
 * has been acquired.</p>
 */
public class IabBroadcastReceiver extends BroadcastReceiver {
  /**
   * The Intent action that this Receiver should filter for.
   */
  public static final String ACTION = "com.android.vending.billing.PURCHASES_UPDATED";
  private final IabBroadcastListener mListener;

  public IabBroadcastReceiver(IabBroadcastListener listener) {
    mListener = listener;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (mListener != null) {
      mListener.receivedBroadcast();
    }
  }

  /**
   * Listener interface for received broadcast messages.
   */
  public interface IabBroadcastListener {
    void receivedBroadcast();
  }
}
