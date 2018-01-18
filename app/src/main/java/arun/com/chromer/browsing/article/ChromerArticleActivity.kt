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

package arun.com.chromer.browsing.article

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager
import android.graphics.Color.WHITE
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import arun.com.chromer.R
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.customtabs.callbacks.ClipboardService
import arun.com.chromer.browsing.customtabs.callbacks.FavShareBroadcastReceiver
import arun.com.chromer.browsing.customtabs.callbacks.SecondaryBrowserReceiver
import arun.com.chromer.browsing.openwith.OpenIntentWithActivity
import arun.com.chromer.browsing.optionspopup.ChromerOptionsActivity
import arun.com.chromer.data.history.DefaultHistoryRepository
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.Preferences.*
import arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_ARTICLE
import arun.com.chromer.shared.Constants.EXTRA_KEY_ORIGINAL_URL
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.SchedulerProvider
import arun.com.chromer.util.Utils
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import javax.inject.Inject

class ChromerArticleActivity : BaseArticleActivity() {
    private var baseUrl: String? = ""

    @Inject
    lateinit var historyRepository: DefaultHistoryRepository

    @Inject
    lateinit var tabsManager: DefaultTabsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseUrl = intent.dataString
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onArticleLoadingFailed(throwable: Throwable?) {
        super.onArticleLoadingFailed(throwable)
        // Loading failed, try to go back to normal url tab if it exists, else start a new normal
        // rendering tab.
        loadInNormalTab()
    }

    override fun onArticleLoaded(webArticle: WebArticle) {
        super.onArticleLoaded(webArticle)
        historyRepository.insert(Website.fromArticle(webArticle))
                .compose(SchedulerProvider.applySchedulers())
                .subscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.article_view_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val actionButton = menu.findItem(R.id.menu_action_button)
        when (Preferences.get(this).preferredAction()) {
            PREFERRED_ACTION_BROWSER -> {
                val browser = Preferences.get(this).secondaryBrowserPackage()
                if (Utils.isPackageInstalled(this, browser)) {
                    actionButton.setTitle(R.string.choose_secondary_browser)
                    val componentName = ComponentName.unflattenFromString(Preferences.get(this).secondaryBrowserComponent()!!)
                    try {
                        actionButton.icon = packageManager.getActivityIcon(componentName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        actionButton.isVisible = false
                    }

                } else {
                    actionButton.isVisible = false
                }
            }
            PREFERRED_ACTION_FAV_SHARE -> {
                val favShare = Preferences.get(this).favSharePackage()
                if (Utils.isPackageInstalled(this, favShare)) {
                    actionButton.setTitle(R.string.fav_share_app)
                    val componentName = ComponentName.unflattenFromString(Preferences.get(this).favShareComponent()!!)
                    try {
                        actionButton.icon = packageManager.getActivityIcon(componentName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        actionButton.isVisible = false
                    }

                } else {
                    actionButton.isVisible = false
                }
            }
            PREFERRED_ACTION_GEN_SHARE -> {
                actionButton.icon = IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_share_variant)
                        .color(WHITE)
                        .sizeDp(24)
                actionButton.setTitle(R.string.share)
            }
        }
        val favoriteShare = menu.findItem(R.id.menu_share_with)
        val pkg = Preferences.get(this).favSharePackage()
        if (pkg != null) {
            val app = Utils.getAppNameWithPackage(this, pkg)
            val label = String.format(getString(R.string.share_with), app)
            favoriteShare.title = label
        } else {
            favoriteShare.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.menu_action_button -> when (Preferences.get(this).preferredAction()) {
                PREFERRED_ACTION_BROWSER -> sendBroadcast(Intent(this, SecondaryBrowserReceiver::class.java).setData(Uri.parse(baseUrl)))
                PREFERRED_ACTION_FAV_SHARE -> sendBroadcast(Intent(this, FavShareBroadcastReceiver::class.java).setData(Uri.parse(baseUrl)))
                PREFERRED_ACTION_GEN_SHARE -> shareUrl()
            }
            R.id.menu_copy_link -> startService(Intent(this, ClipboardService::class.java).setData(Uri.parse(baseUrl)))
            R.id.menu_open_with -> startActivity(Intent(this, OpenIntentWithActivity::class.java).setData(Uri.parse(baseUrl)))
            R.id.menu_share -> shareUrl()
            R.id.menu_open_full_page -> loadInNormalTab()
            R.id.menu_more -> {
                val moreMenuActivity = Intent(this, ChromerOptionsActivity::class.java)
                        .apply {
                            data = intent.data
                            putExtra(EXTRA_KEY_ORIGINAL_URL, baseUrl)
                            putExtra(EXTRA_KEY_FROM_ARTICLE, true)
                            addFlags(FLAG_ACTIVITY_NEW_TASK)
                            addFlags(FLAG_ACTIVITY_NEW_DOCUMENT)
                            addFlags(FLAG_ACTIVITY_MULTIPLE_TASK)
                        }
                startActivity(moreMenuActivity)
            }
            R.id.menu_share_with -> sendBroadcast(Intent(this, FavShareBroadcastReceiver::class.java).setData(Uri.parse(baseUrl)))
        }
        return true
    }

    private fun loadInNormalTab() {
        finish()
        if (intent != null && intent.dataString != null) {
            tabsManager.openBrowsingTab(this, Website(intent.dataString!!), true, false, CustomTabActivity::class.java.name)
        }
    }

    private fun shareUrl() {
        Utils.shareText(this, baseUrl)
    }
}
