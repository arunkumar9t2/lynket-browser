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

package arun.com.chromer.browsing.customtabs.callbacks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import arun.com.chromer.Lynket
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.tabs.TabsManager
import timber.log.Timber
import javax.inject.Inject

class MinimizeBroadcastReceiver : BroadcastReceiver() {

  @Inject
  lateinit var tabsManager: TabsManager

  override fun onReceive(context: Context, intent: Intent) {
    (context.applicationContext as Lynket).appComponent.inject(this)
    val url = intent.getStringExtra(Constants.EXTRA_KEY_ORIGINAL_URL)
    if (url != null) {
      tabsManager.minimizeTabByUrl(url, CustomTabActivity::class.java.name)
    } else {
      Timber.e("Error minimizing")
    }
  }
}
