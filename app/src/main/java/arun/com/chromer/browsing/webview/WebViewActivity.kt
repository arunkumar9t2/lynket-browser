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

package arun.com.chromer.browsing.webview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.WHITE
import android.os.Build
import android.os.Bundle
import android.view.InflateException
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebViewClient
import android.widget.Toast
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.browsing.customtabs.callbacks.ClipboardService
import arun.com.chromer.browsing.customtabs.callbacks.FavShareBroadcastReceiver
import arun.com.chromer.browsing.customtabs.callbacks.SecondaryBrowserReceiver
import arun.com.chromer.browsing.openwith.OpenIntentWithActivity
import arun.com.chromer.browsing.optionspopup.ChromerOptionsActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.Preferences.*
import arun.com.chromer.shared.Constants.EXTRA_KEY_ORIGINAL_URL
import arun.com.chromer.util.Utils
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.content_web_view.*
import timber.log.Timber

class WebViewActivity : BrowsingActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setSupportActionBar(toolbar)

            if (supportActionBar != null) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.setHomeAsUpIndicator(xyz.klinker.android.article.R.drawable.article_ic_close)
                supportActionBar!!.title = website?.safeLabel() ?: intent.dataString
            }

            web_view.webViewClient = object : WebViewClient() {

            }
            val webSettings = web_view!!.settings
            webSettings.javaScriptEnabled = true

            web_view.loadUrl(intent.dataString)
        } catch (e: InflateException) {
            Timber.e(e)
            Toast.makeText(this, R.string.web_view_not_found, Toast.LENGTH_LONG).show()
            finish()
        }

    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_web_view
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
        val fullPage = menu.findItem(R.id.menu_open_full_page)
        fullPage.isVisible = false
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.menu_action_button -> when (Preferences.get(this).preferredAction()) {
                PREFERRED_ACTION_BROWSER -> sendBroadcast(Intent(this, SecondaryBrowserReceiver::class.java).setData(intent.data))
                PREFERRED_ACTION_FAV_SHARE -> sendBroadcast(Intent(this, FavShareBroadcastReceiver::class.java).setData(intent.data))
                PREFERRED_ACTION_GEN_SHARE -> shareUrl()
            }
            R.id.menu_copy_link -> startService(Intent(this, ClipboardService::class.java).setData(intent.data))
            R.id.menu_open_with -> startActivity(Intent(this, OpenIntentWithActivity::class.java).setData(intent.data))
            R.id.menu_share -> shareUrl()
            R.id.menu_more -> {
                val moreMenuActivity = Intent(this, ChromerOptionsActivity::class.java).apply {
                    data = intent.data
                    putExtra(EXTRA_KEY_ORIGINAL_URL, intent.dataString)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                }
                startActivity(moreMenuActivity)
            }
            R.id.menu_share_with -> sendBroadcast(Intent(this, FavShareBroadcastReceiver::class.java).setData(intent.data))
        }
        return true
    }

    private fun shareUrl() {
        Utils.shareText(this, intent.dataString)
    }

    override fun onDestroy() {
        web_view.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onWebsiteLoaded(website: Website) {

    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }
}
