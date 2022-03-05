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

package arun.com.chromer.browsing.customtabs

import android.os.Bundle
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.shared.Constants.EXTRA_KEY_TOOLBAR_COLOR

class CustomTabActivity : BrowsingActivity() {
  /**
   * As soon as user presses back, this activity will get focus. We need to kill this activity else
   * user will see a ghost tab.
   */
  private var isLoaded = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val toolbarColor = intent.getIntExtra(EXTRA_KEY_TOOLBAR_COLOR, 0)

    if (savedInstanceState == null) {
      activityComponent.customTabs()
        .forUrl(intent.dataString!!)
        .toolbarColor(toolbarColor)
        .launch()
    } else finish()
  }

  override fun inject(activityComponent: ActivityComponent) {
    activityComponent.inject(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    isLoaded = true
  }

  override fun onResume() {
    super.onResume()
    if (isLoaded) {
      finish()
    }
  }

  override fun onWebsiteLoaded(website: Website) {

  }

  override val layoutRes: Int get() = 0
}
