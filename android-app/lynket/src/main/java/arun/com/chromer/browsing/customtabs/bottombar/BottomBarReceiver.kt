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

package arun.com.chromer.browsing.customtabs.bottombar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import arun.com.chromer.Lynket
import arun.com.chromer.R
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.shared.Constants
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.Utils
import timber.log.Timber
import javax.inject.Inject

class BottomBarReceiver : BroadcastReceiver() {
  @Inject
  lateinit var tabsManager: TabsManager

  override fun onReceive(context: Context, intent: Intent) {
    (context.applicationContext as Lynket).appComponent.inject(this)

    val clickedId = intent.getIntExtra(CustomTabsIntent.EXTRA_REMOTEVIEWS_CLICKED_ID, -1)
    val url = intent.dataString
    val orgUrl = intent.getStringExtra(Constants.EXTRA_KEY_ORIGINAL_URL)
    if (url == null || clickedId == -1) {
      Timber.d("Skipped bottom bar callback")
      return
    }

    when (clickedId) {
      R.id.bottom_bar_open_in_new_tab -> OpenInNewTab(context, url).perform()
      R.id.bottom_bar_share -> ShareUrl(context, url).perform()
      R.id.bottom_bar_tabs -> TabsScreen(context, url).perform()
      R.id.bottom_bar_minimize_tab -> orgUrl?.let { MinimizeUrl(context, orgUrl).perform() }
      R.id.bottom_bar_article_view -> ArticleView(context, url).perform()
    }
  }

  abstract inner class Command internal constructor(context: Context, internal val url: String) {
    internal var context: Context? = null
    internal var performCalled = false

    init {
      this.context = context.applicationContext
    }

    internal fun perform() {
      performCalled = true
      onPerform()
      context = null
    }

    protected abstract fun onPerform()
  }

  inner class ArticleView internal constructor(context: Context, url: String) :
    Command(context, url) {

    override fun onPerform() {
      if (!performCalled) {
        throw IllegalStateException("Should call perform() instead of onPerform()")
      }
      tabsManager.openArticle(context!!, Website(url), true)
    }
  }

  inner class OpenInNewTab internal constructor(context: Context, url: String) :
    Command(context, url) {

    override fun onPerform() {
      if (!performCalled) {
        throw IllegalStateException("Should call perform() instead of onPerform()")
      }
      tabsManager.openNewTab(context!!, url)
    }

  }

  inner class ShareUrl internal constructor(context: Context, url: String) : Command(context, url) {

    override fun onPerform() {
      Utils.shareText(context!!, url)
    }
  }

  inner class MinimizeUrl internal constructor(context: Context, orgUrl: String) :
    Command(context, orgUrl) {

    override fun onPerform() {
      tabsManager.minimizeTabByUrl(url, CustomTabActivity::class.java.name)
    }
  }

  inner class TabsScreen internal constructor(context: Context, orgUrl: String) :
    Command(context, orgUrl) {

    override fun onPerform() {
      tabsManager.showTabsActivity()
    }
  }
}
