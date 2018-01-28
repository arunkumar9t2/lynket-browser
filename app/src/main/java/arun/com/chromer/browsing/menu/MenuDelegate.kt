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

package arun.com.chromer.browsing.menu

import android.app.Activity
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.di.scopes.PerActivity
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.Utils.shareText
import javax.inject.Inject

/**
 * Created by arunk on 28-01-2018.
 * Delegate to handle common menu functionality across browsing activities.
 */
@PerActivity
class MenuDelegate @Inject constructor(
        val activity: Activity,
        val tabsManager: DefaultTabsManager
) {
    /**
     * Currently active URL.
     */
    private val currentUrl: String
        get() = (activity as BrowsingActivity).getCurrentUrl()

    fun handleMenuClick(itemId: Int) {
        when (itemId) {
            R.id.bottom_bar_open_in_new_tab -> {
                tabsManager.openNewTab(activity, currentUrl)
            }
            R.id.bottom_bar_share -> {
                shareText(activity, currentUrl)
            }
            R.id.bottom_bar_tabs -> {
                tabsManager.showTabsActivity()
            }
            R.id.bottom_bar_minimize_tab -> {
                tabsManager.minimizeTabByUrl(currentUrl)
            }
        }
    }
}
