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

package arun.com.chromer.tabs

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.widget.Toast
import arun.com.chromer.BuildConfig
import arun.com.chromer.R
import arun.com.chromer.appdetect.AppDetectionManager
import arun.com.chromer.browsing.amp.AmpResolverActivity
import arun.com.chromer.browsing.article.ArticleActivity
import arun.com.chromer.browsing.article.ArticlePreloader
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.customtabs.CustomTabs
import arun.com.chromer.browsing.newtab.NewTabDialogActivity
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.data.apps.AppRepository
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.isPackageInstalled
import arun.com.chromer.settings.Preferences
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.Constants.NO_COLOR
import arun.com.chromer.tabs.ui.TabsActivity
import arun.com.chromer.util.DocumentUtils
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.SafeIntent
import arun.com.chromer.util.Utils
import arun.com.chromer.util.Utils.openDrawOverlaySettings
import arun.com.chromer.webheads.WebHeadService
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import rx.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTabsManager
@Inject
constructor(
        val application: Application,
        val preferences: Preferences,
        private val appDetectionManager: AppDetectionManager,
        private val appRepository: AppRepository,
        private val websiteRepository: WebsiteRepository,
        private val articlePreloader: ArticlePreloader,
        val rxEventBus: RxEventBus
) : TabsManager {

    override fun openUrl(context: Context, website: Website, fromApp: Boolean, fromWebHeads: Boolean, fromNewTab: Boolean) {
        // Clear non browsing activities if it was external intent.
        if (!fromApp) {
            clearNonBrowsingActivities()
        }
        // Open in web heads mode if we this command did not come from web heads.
        if (preferences.webHeads() && !fromWebHeads) {
            openWebHeads(context, website.preferredUrl())
            return
        }

        // Check if already an instance for this URL is there in our tasks
        if (reOrderTabByUrl(context, website)) {
            // Just bring it to front
            return
        }

        // Check if we should try to find AMP version of incoming url.
        if (preferences.ampMode()) {
            if (website.hasAmp()) {
                // We already got the amp url, so open it in a browsing tab.
                openBrowsingTab(context, Website.Ampify(website), fromNewTab = fromNewTab)
            } else {
                // Open a proxy activity, attempt an extraction then open the AMP url if exists.
                val ampResolver = Intent(context, AmpResolverActivity::class.java).apply {
                    data = website.preferredUri()
                    if (context !is Activity) {
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                context.startActivity(ampResolver)
            }
            return
        }

        if (preferences.articleMode()) {
            // Launch article mode
            openArticle(context, website)
            return
        }

        // If everything failed then launch normally in browsing activity.
        openBrowsingTab(context, website, fromNewTab = fromNewTab)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun reOrderTabByUrl(context: Context, website: Website, activityName: String?): Boolean {
        return findTaskAndExecuteAction(context, website, activityName, { task ->
            Timber.d("Moved tab to front $website")
            task.moveToFront()
        })
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun finishTabByUrl(context: Context, website: Website, activityName: String?): Boolean {
        return findTaskAndExecuteAction(context, website, activityName, { task ->
            Timber.d("Finishing task $website")
            task.finishAndRemoveTask()
        })
    }

    /**
     * Talks to activity manager and finds all active task. Then find a task that matches the input
     * criteria which is base url and optionally and preferred activity name the url belongs to.
     * Upon finding the task, executes {@param foundAction}
     */
    private fun findTaskAndExecuteAction(context: Context, website: Website, activityName: String?,
                                         foundAction: (task: ActivityManager.AppTask) -> Unit): Boolean {
        try {
            val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            if (Utils.isLollipopAbove()) {
                for (task in am.appTasks) {
                    val info = DocumentUtils.getTaskInfoFromTask(task)
                    info?.let {
                        try {
                            val intent = info.baseIntent
                            val url = intent.dataString
                            val componentClassName = intent.component!!.className

                            val urlMatches = url != null && (url.equals(website.url, ignoreCase = true)
                                    || url.equals(website.preferredUrl(), ignoreCase = true)
                                    || url.equals(website.ampUrl, ignoreCase = true))

                            val taskComponentMatches = if (activityName != null) {
                                componentClassName == activityName
                            } else {
                                (componentClassName == CustomTabActivity::class.java.name
                                        || componentClassName == ArticleActivity::class.java.name
                                        || componentClassName == WebViewActivity::class.java.name)
                            }

                            if (taskComponentMatches && urlMatches) {
                                foundAction(task)
                                return true
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return false
    }

    override fun minimizeTabByUrl(url: String) {
        rxEventBus.post(TabsManager.MinimizeEvent(url))
        if (preferences.webHeads()) {
            // When minimizing, don't try to handle aggressive loading cases.
            openWebHeads(application, url, fromMinimize = true)
        }
    }

    override fun processIncomingIntent(activity: Activity, intent: Intent) {
        // Safety check against malicious intents
        val safeIntent = SafeIntent(intent)
        val url = safeIntent.dataString

        // The first thing to check is if we should blacklist.
        if (preferences.blacklist()) {
            val lastApp = appDetectionManager.nonFilteredPackage
            if (lastApp.isNotEmpty() && appRepository.isPackageBlacklisted(lastApp)) {
                doBlacklistAction(activity, safeIntent)
                return
            }
        }

        // Open url normally
        openUrl(activity, Website(url), fromApp = false)
    }

    override fun openArticle(context: Context, website: Website, newTab: Boolean) {
        if (!reOrderTabByUrl(context, website, ArticleActivity::class.java.name)) {
            val intent = Intent(context, ArticleActivity::class.java).apply {
                data = website.preferredUri()
                if (context !is Activity) {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
                if (newTab || Preferences.get(context).mergeTabs()) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    addFlags(FLAG_ACTIVITY_MULTIPLE_TASK)
                }
                putExtra(Constants.EXTRA_KEY_TOOLBAR_COLOR, getToolbarColor(website))
            }
            context.startActivity(intent)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun openBrowsingTab(context: Context, website: Website, smart: Boolean, fromNewTab: Boolean, activityName: String?) {
        val reordered = smart && reOrderTabByUrl(context, website, activityName)

        if (!reordered) {
            val canSafelyOpenCCT = CustomTabs.getCustomTabSupportingPackages(context).isNotEmpty()
            val isIncognito = preferences.incognitoMode() && false // Disable auto revert to webview for now.

            val browsingActivity = if (!isIncognito && canSafelyOpenCCT) {
                Intent(context, CustomTabActivity::class.java)
            } else {
                Intent(context, WebViewActivity::class.java)
            }.apply {
                data = website.preferredUri()
                putExtra(Constants.EXTRA_KEY_WEBSITE, website)
                putExtra(Constants.EXTRA_KEY_TOOLBAR_COLOR, getToolbarColor(website))
            }

            if (preferences.mergeTabs() || fromNewTab) {
                browsingActivity.addFlags(FLAG_ACTIVITY_NEW_DOCUMENT)
                browsingActivity.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            if (context !is Activity) {
                browsingActivity.addFlags(FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browsingActivity)
        }
    }

    override fun openWebHeads(context: Context, url: String, fromMinimize: Boolean) {
        if (Utils.isOverlayGranted(context)) {
            val webHeadLauncher = Intent(context, WebHeadService::class.java).apply {
                data = Uri.parse(url)
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            }
            ContextCompat.startForegroundService(context, webHeadLauncher)
        } else {
            openDrawOverlaySettings(context)
        }

        // If this command was not issued for minimizing, then attempt aggressive loading.
        if (preferences.aggressiveLoading() && !fromMinimize) {
            if (preferences.articleMode()) {
                articlePreloader.preloadArticle(Uri.parse(url), { })
            } else {
                // Register listener to track opening browsing tabs.
                application.registerActivityLifecycleCallbacks(
                        object : ActivityLifeCycleCallbackAdapter() {
                            override fun onActivityStopped(activity: Activity?) {
                                // Let's inspect this activity and find if it's what we are looking for.
                                try {
                                    if (activity != null) {
                                        val activityClass = activity.javaClass.name
                                        if (activityClass == CustomTabActivity::class.java.name
                                                || activityClass == WebViewActivity::class.java.name) {

                                            val activityUrl = activity.intent?.dataString

                                            if (url == activityUrl) {
                                                Timber.d("Found activity $activityClass.")

                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    activity.moveTaskToBack(true)
                                                    Timber.d("Moved ${activityClass + activityUrl} to back")
                                                    // Unregister this callback
                                                    application.unregisterActivityLifecycleCallbacks(this)
                                                }, 100)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e)
                                }
                            }
                        })

                openBrowsingTab(context, Website(url), smart = true, fromNewTab = false)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun openNewTab(context: Context, url: String) {
        val newTabIntent = Intent(context, NewTabDialogActivity::class.java).apply {
            data = Uri.parse(url)
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            addFlags(FLAG_ACTIVITY_NEW_DOCUMENT)
            addFlags(FLAG_ACTIVITY_MULTIPLE_TASK)
        }
        context.startActivity(newTabIntent)
    }

    override fun clearNonBrowsingActivities() {
        rxEventBus.post(TabsManager.FinishRoot())
    }

    override fun showTabsActivity() {
        application.startActivity(Intent(application, TabsActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
        })
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getActiveTabs(): Single<List<TabsManager.Tab>> {
        return Single.create({ onSubscribe ->
            try {
                val am = application.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                onSubscribe.onSuccess(am.appTasks!!
                        .map { DocumentUtils.getTaskInfoFromTask(it) }
                        .filter { it != null && it.baseIntent?.dataString != null && it.baseIntent.component != null }
                        .map {
                            val url = it.baseIntent.dataString
                            val type = when (it.baseIntent.component.className) {
                                CustomTabActivity::class.java.name -> CUSTOM_TAB
                                WebViewActivity::class.java.name -> WEB_VIEW
                                ArticleActivity::class.java.name -> ARTICLE
                                else -> OTHER
                            }
                            TabsManager.Tab(url, type)
                        }.filter { it.type != OTHER }
                        .toMutableList())
            } catch (e: Exception) {
                onSubscribe.onError(e)
            }
        })
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun closeAllTabs(): Single<List<TabsManager.Tab>> {
        return getActiveTabs()
                .toObservable()
                .flatMapIterable { it }
                .map {
                    finishTabByUrl(application, Website(it.url), it.getTargetActivityName())
                    it
                }.toList()
                .toSingle()
    }

    /**
     * Get customized toolbar color based on user preferences
     */
    @ColorInt
    private fun getToolbarColor(website: Website): Int {
        if (preferences.isColoredToolbar) {
            val toolbarColor = if (preferences.dynamicToolbar()) {
                var toolbarColor = Constants.NO_COLOR
                if (preferences.dynamicToolbarOnApp()) {
                    toolbarColor = appRepository.getPackageColorSync(appDetectionManager.filteredPackage)
                }
                if (preferences.dynamicToolbarOnWeb()) {
                    toolbarColor = websiteRepository.getWebsiteColorSync(website.url)
                    if (toolbarColor == Constants.NO_COLOR) {
                        toolbarColor = website.themeColor()
                    }
                }
                toolbarColor
            } else {
                preferences.toolbarColor()
            }
            return if (toolbarColor != NO_COLOR) {
                toolbarColor
            } else {
                preferences.toolbarColor()
            }
        } else return ContextCompat.getColor(application, R.color.primary)
    }

    /**
     * Performs the blacklist action which is opening the given url in user's secondary browser.
     */
    private fun doBlacklistAction(activity: Activity, safeIntent: SafeIntent) {
        // Perform a safe copy of this intent
        val intentCopy = Intent().apply {
            data = safeIntent.data
            safeIntent.unsafe.extras?.let {
                putExtras(it)
            }
        }
        val secondaryBrowser = preferences.secondaryBrowserPackage()
        if (secondaryBrowser == null) {
            showSecondaryBrowserHandlingError(activity, activity.getText(R.string.secondary_browser_not_error))
            return
        }
        if (activity.packageManager.isPackageInstalled(secondaryBrowser)) {
            intentCopy.`package` = secondaryBrowser
            try {
                activity.startActivity(intentCopy)
                if (BuildConfig.DEBUG) {
                    Toast.makeText(activity, "Blacklisted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                showSecondaryBrowserHandlingError(activity, activity.getText(R.string.secondary_browser_launch_error))
            }
        } else {
            showSecondaryBrowserHandlingError(activity, activity.getText(R.string.secondary_browser_not_installed))
        }
    }

    /**
     * Shows a error dialog for various blacklist errors.
     */
    private fun showSecondaryBrowserHandlingError(activity: Activity, message: CharSequence) {
        MaterialDialog.Builder(activity)
                .title(R.string.secondary_browser_launching_error_title)
                .content(message)
                .iconRes(R.mipmap.ic_launcher)
                .positiveText(R.string.launch_setting)
                .negativeText(android.R.string.cancel)
                .theme(Theme.LIGHT)
                .positiveColorRes(R.color.colorAccent)
                .negativeColorRes(R.color.colorAccent)
                .onPositive { _, _ ->
                    val chromerIntent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
                    chromerIntent!!.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    activity.startActivity(chromerIntent)
                }
                .dismissListener({ activity.finish() }).show()
    }

    /**
     * Adapter to let us implement only what's needed from the interface.
     */
    open class ActivityLifeCycleCallbackAdapter : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity?) {}

        override fun onActivityResumed(activity: Activity?) {}

        override fun onActivityStarted(activity: Activity?) {}

        override fun onActivityDestroyed(activity: Activity?) {}

        override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) {}

        override fun onActivityStopped(activity: Activity?) {}

        override fun onActivityCreated(activity: Activity?, bundle: Bundle?) {}
    }
}