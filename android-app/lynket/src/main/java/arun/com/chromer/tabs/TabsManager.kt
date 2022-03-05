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

package arun.com.chromer.tabs

import android.app.Activity
import android.content.Context
import android.content.Intent
import arun.com.chromer.browsing.article.ArticleActivity
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.webview.EmbeddableWebViewActivity
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.data.website.model.Website
import io.reactivex.Completable
import rx.Single

/**
 * Helper class to manage tabs opened by Lynket. Responsible for managing Lynket's task stack.
 */
interface TabsManager {

  companion object {
    val CUSTOM_TAB_ACTIVITY: String = CustomTabActivity::class.java.name
    val ARTICLE_ACTIVITY: String = ArticleActivity::class.java.name
    val WEBVIEW_ACTIVITY: String = WebViewActivity::class.java.name
    val EMBEDDABLE_WEBVIEW_ACTIVITY: String = EmbeddableWebViewActivity::class.java.name

    val ALL_BROWSING_ACTIVITIES = arrayListOf(
      CUSTOM_TAB_ACTIVITY,
      ARTICLE_ACTIVITY,
      WEBVIEW_ACTIVITY,
      EMBEDDABLE_WEBVIEW_ACTIVITY
    )
    val FULL_BROWSING_ACTIVITIES = arrayListOf(
      CUSTOM_TAB_ACTIVITY,
      WEBVIEW_ACTIVITY,
      EMBEDDABLE_WEBVIEW_ACTIVITY
    )
  }

  // Event for closing non browsing activity.
  class FinishRoot

  // Event for minimize command.
  data class MinimizeEvent(val tab: Tab)

  data class Tab(val url: String, @param:TabType var type: Int, var website: Website? = null) {
    fun getTargetActivityName(): String = when (type) {
      WEB_VIEW -> WebViewActivity::class.java.name
      WEB_VIEW_EMBEDDED -> EmbeddableWebViewActivity::class.java.name
      CUSTOM_TAB -> CustomTabActivity::class.java.name
      ARTICLE -> ArticleActivity::class.java.name
      else -> CustomTabActivity::class.java.name
    }
  }

  @TabType
  fun getTabType(className: String): Int = when (className) {
    CustomTabActivity::class.java.name -> CUSTOM_TAB
    WebViewActivity::class.java.name -> WEB_VIEW
    EmbeddableWebViewActivity::class.java.name -> WEB_VIEW_EMBEDDED
    ArticleActivity::class.java.name -> ARTICLE
    else -> OTHER
  }

  /**
   * Takes a {@param website} and opens in based on user preference. Checks for web heads, amp,
   * article, reordering existing tabs etc.
   */
  fun openUrl(
    context: Context,
    website: Website,
    fromApp: Boolean = true,
    fromWebHeads: Boolean = false,
    fromNewTab: Boolean = false,
    fromAmp: Boolean = false,
    incognito: Boolean = false
  )

  /**
   * Opens the given Uri in a browsing tab.
   */
  fun openBrowsingTab(
    context: Context,
    website: Website,
    smart: Boolean = false,
    fromNewTab: Boolean,
    activityNames: List<String>? = null,
    incognito: Boolean = false
  )

  /**
   * Closes all browsing tabs present in our process.
   */
  fun closeAllTabs(): Single<List<Tab>>

  /**
   * Returns true if it is determined that we already have any of our browsing activity has opened
   * this url.
   *
   * Optionally specify which Activity class should be brought to front
   */
  fun reOrderTabByUrl(
    context: Context,
    website: Website,
    activityNames: List<String>? = null
  ): Boolean

  /**
   * Same as {@link reOrderTabByUrl} but instead of reordering, finishes and removes the task.
   */
  fun finishTabByUrl(
    context: Context,
    website: Website,
    activityNames: List<String>? = null
  ): Boolean

  /**
   * If a task exist with this url already then this method should minimize it a.k.a putting it in
   * the background task.
   *
   * After that, an attempt to open web heads is made if it is enabled.
   */
  fun minimizeTabByUrl(url: String, fromClass: String, incognito: Boolean = false)

  /**
   * Processes incoming intent from preferably external apps (could be us too) and then figures out
   * how to handle the intent and launch a new url.
   */
  fun processIncomingIntent(activity: Activity, intent: Intent): Completable

  /**
   * Opens the given {@param url} irrespective of whether web heads is on.
   *
   * After opening the web heads, an attempt to handle aggressive background loading is attempted
   * if {@param fromMinimize} is {@code false}
   */
  fun openWebHeads(
    context: Context,
    website: Website,
    fromMinimize: Boolean = false,
    fromAmp: Boolean = false,
    incognito: Boolean = false
  )

  /**
   * Opens new tab activity.
   */
  fun openNewTab(context: Context, url: String)

  /**
   * Clear non browsing activities so that pressing back returns user to the app that launched
   * the browsing tab.
   *
   */
  fun clearNonBrowsingActivities()

  /**
   * Get active tabs serving an url.
   */
  fun getActiveTabs(): Single<List<Tab>>

  /**
   * Show tabs activity
   */
  fun showTabsActivity()

  fun openArticle(
    context: Context,
    website: Website,
    newTab: Boolean = false,
    incognito: Boolean = false
  )

  fun shouldUseWebView(incognito: Boolean): Boolean
}
