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
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.browsing.article.ArticleActivity
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.customtabs.callbacks.CopyToClipboardService
import arun.com.chromer.browsing.customtabs.callbacks.FavShareBroadcastReceiver
import arun.com.chromer.browsing.customtabs.callbacks.SecondaryBrowserReceiver
import arun.com.chromer.browsing.openwith.OpenIntentWithActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.scopes.PerActivity
import arun.com.chromer.history.HistoryActivity
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.Preferences.*
import arun.com.chromer.shortcuts.HomeScreenShortcutCreatorActivity
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.Utils
import arun.com.chromer.util.Utils.isPackageInstalled
import arun.com.chromer.util.Utils.shareText
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import javax.inject.Inject

/**
 * Created by arunk on 28-01-2018.
 * Delegate to handle common menu functionality across browsing activities.
 */
@PerActivity
class MenuDelegate @Inject constructor(
        val activity: Activity,
        val tabsManager: DefaultTabsManager,
        val preferences: Preferences
) {
    /**
     * Currently active URL.
     */
    private val currentUrl: String
        get() = (activity as BrowsingActivity).getCurrentUrl()
    private val currentUri: Uri
        get() = Uri.parse(currentUrl)

    private val isArticle = activity is ArticleActivity

    fun createOptionsMenu(menu: Menu): Boolean {
        with(menu) {
            if (isArticle) {
                add(0, R.id.menu_open_full_page, Menu.NONE, R.string.open_full_page).apply {
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    setIcon(R.drawable.ic_open_in_browser_36dp)
                }
            }
            add(0, R.id.menu_action_button, Menu.NONE, R.string.choose_secondary_browser).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
            add(0, R.id.menu_copy_link, Menu.NONE, R.string.copy_link)
            add(0, R.id.menu_open_with, Menu.NONE, R.string.open_with)
            add(0, R.id.menu_share, Menu.NONE, R.string.share)
            add(0, R.id.menu_share_with, Menu.NONE, "")
            add(0, R.id.menu_history, Menu.NONE, R.string.title_history)
            add(0, R.id.menu_add_to_home_screen, Menu.NONE, R.string.add_to_homescreen)
            if (!preferences.bottomBar()) {
                add(0, R.id.tabs, Menu.NONE, R.string.title_tabs)
            }
        }
        return true
    }

    fun prepareOptionsMenu(menu: Menu): Boolean {
        val actionButton = menu.findItem(R.id.menu_action_button)
        when (preferences.preferredAction()) {
            PREFERRED_ACTION_BROWSER -> {
                val secondaryBrowserPackage = preferences.secondaryBrowserPackage()
                if (isPackageInstalled(activity, secondaryBrowserPackage)) {
                    actionButton.setTitle(R.string.choose_secondary_browser)
                    val componentName = ComponentName.unflattenFromString(preferences.secondaryBrowserComponent()!!)
                    try {
                        actionButton.icon = activity.packageManager.getActivityIcon(componentName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        actionButton.isVisible = false
                    }
                } else {
                    actionButton.isVisible = false
                }
            }
            PREFERRED_ACTION_FAV_SHARE -> {
                val favSharePackage = preferences.favSharePackage()
                if (isPackageInstalled(activity, favSharePackage)) {
                    actionButton.setTitle(R.string.fav_share_app)
                    val componentName = ComponentName.unflattenFromString(preferences.favShareComponent()!!)
                    try {
                        actionButton.icon = activity.packageManager.getActivityIcon(componentName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        actionButton.isVisible = false
                    }
                } else {
                    actionButton.isVisible = false
                }
            }
            PREFERRED_ACTION_GEN_SHARE -> {
                actionButton.icon = IconicsDrawable(activity).apply {
                    icon(CommunityMaterial.Icon.cmd_share_variant)
                    color(Color.WHITE)
                    sizeDp(24)
                }
                actionButton.setTitle(R.string.share)
            }
        }
        val favoriteShare = menu.findItem(R.id.menu_share_with)
        val favSharePackage = preferences.favSharePackage()
        if (favSharePackage != null) {
            val app = Utils.getAppNameWithPackage(activity, favSharePackage)
            val label = String.format(activity.getString(R.string.share_with), app)
            favoriteShare.title = label
        } else {
            favoriteShare.isVisible = false
        }
        return true
    }

    private fun shareUrl() {
        shareText(activity, currentUrl)
    }

    fun handleItemSelected(itemId: Int): Boolean {
        when (itemId) {
            R.id.menu_open_full_page -> tabsManager.openBrowsingTab(activity, Website(currentUrl), true, false, CustomTabActivity::class.java.name)
            android.R.id.home -> activity.finish()
            R.id.bottom_bar_open_in_new_tab -> tabsManager.openNewTab(activity, currentUrl)
            R.id.bottom_bar_share -> shareUrl()
            R.id.bottom_bar_tabs, R.id.tabs -> tabsManager.showTabsActivity()
            R.id.bottom_bar_minimize_tab -> tabsManager.minimizeTabByUrl(currentUrl)
            R.id.menu_action_button -> when (preferences.preferredAction()) {
                PREFERRED_ACTION_BROWSER -> activity.sendBroadcast(Intent(activity, SecondaryBrowserReceiver::class.java).setData(currentUri))
                PREFERRED_ACTION_FAV_SHARE -> activity.sendBroadcast(Intent(activity, FavShareBroadcastReceiver::class.java).setData(currentUri))
                PREFERRED_ACTION_GEN_SHARE -> shareUrl()
            }
            R.id.menu_copy_link -> activity.startService(Intent(activity, CopyToClipboardService::class.java).setData(currentUri))
            R.id.menu_open_with -> activity.startActivity(Intent(activity, OpenIntentWithActivity::class.java).setData(currentUri))
            R.id.menu_share -> shareUrl()
            R.id.menu_share_with -> activity.sendBroadcast(Intent(activity, FavShareBroadcastReceiver::class.java).setData(currentUri))
            R.id.menu_history -> activity.startActivity(Intent(activity, HistoryActivity::class.java))
            R.id.menu_add_to_home_screen -> activity.startActivity(Intent(activity, HomeScreenShortcutCreatorActivity::class.java).setData(currentUri))
        }
        return true
    }
}
