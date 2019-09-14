/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

import android.annotation.SuppressLint
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
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import arun.com.chromer.BuildConfig
import arun.com.chromer.R
import arun.com.chromer.appdetect.AppDetectionManager
import arun.com.chromer.browsing.amp.AmpResolverActivity
import arun.com.chromer.browsing.article.ArticleActivity
import arun.com.chromer.browsing.backgroundloading.BackgroundLoadingStrategyFactory
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.customtabs.CustomTabs
import arun.com.chromer.browsing.newtab.NewTabDialogActivity
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.bubbles.BubbleType.NATIVE
import arun.com.chromer.bubbles.BubbleType.WEB_HEADS
import arun.com.chromer.bubbles.FloatingBubbleFactory
import arun.com.chromer.data.apps.AppRepository
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.isPackageInstalled
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.RxPreferences
import arun.com.chromer.shared.Constants.*
import arun.com.chromer.tabs.ui.TabsActivity
import arun.com.chromer.util.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import io.reactivex.Completable
import rx.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTabsManager
@Inject
constructor(
        private val application: Application,
        private val preferences: Preferences,
        private val appDetectionManager: AppDetectionManager,
        private val appRepository: AppRepository,
        private val websiteRepository: WebsiteRepository,
        private val backgroundLoadingStrategyFactory: BackgroundLoadingStrategyFactory,
        private val rxEventBus: RxEventBus,
        private val floatingBubbleFactory: FloatingBubbleFactory,
        private val rxPreferences: RxPreferences,
        private val schedulerProvider: SchedulerProvider
) : TabsManager {

    override fun openUrl(
            context: Context,
            website: Website,
            fromApp: Boolean, // TODO Move to sealed classes
            fromWebHeads: Boolean,
            fromNewTab: Boolean,
            fromAmp: Boolean,
            incognito: Boolean
    ) {
        openUrlInternal(
                context,
                website,
                fromApp,
                fromWebHeads,
                fromNewTab,
                fromAmp,
                incognito
        ).subscribeOn(schedulerProvider.pool)
                .subscribe()
    }

    private fun openUrlInternal(
            context: Context,
            website: Website,
            fromApp: Boolean = true,
            fromWebHeads: Boolean = false,
            fromNewTab: Boolean = false,
            fromAmp: Boolean = false,
            incognito: Boolean = false
    ): Completable = Completable.fromAction {
        // Clear non browsing activities if it was external intent.
        if (!fromApp) {
            clearNonBrowsingActivities()
        }
        // Open in web heads mode if we this command did not come from web heads.
        if ((preferences.webHeads() || rxPreferences.nativeBubbles.get()) && !fromWebHeads) {
            openWebHeads(context, website = website, fromMinimize = fromAmp, incognito = incognito)
            return@fromAction
        }

        // Check if already an instance for this URL is there in our tasks
        if (reOrderTabByUrl(context, website)) {
            // Just bring it to front
            return@fromAction
        }

        // Check if we should try to find AMP version of incoming url.
        if (preferences.ampMode() && !fromAmp) {
            if (website.hasAmp()) {
                // We already got the amp url, so open it in a browsing tab.
                openBrowsingTab(context, Website.Ampify(website), fromNewTab = fromNewTab, incognito = incognito)
                return@fromAction
            } else if (!fromWebHeads) {
                // Open a proxy activity, attempt an extraction then open the AMP url if exists.
                val ampResolver = Intent(context, AmpResolverActivity::class.java).apply {
                    data = website.preferredUri()
                    if (context !is Activity) {
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (incognito) {
                        putExtra(EXTRA_KEY_INCOGNITO, true)
                    }
                }
                context.startActivity(ampResolver)
                return@fromAction
            }
        }

        if (preferences.articleMode()) {
            // Launch article mode
            openArticle(context, website, incognito = incognito)
            return@fromAction
        }
        // If everything failed then launch normally in browsing activity.
        openBrowsingTab(context, website, fromNewTab = fromNewTab, incognito = incognito)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun reOrderTabByUrl(context: Context, website: Website, activityNames: List<String>?): Boolean {
        return findTaskAndExecuteAction(context, website, activityNames) { task ->
            Timber.d("Moved tab to front $website")
            task.moveToFront()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun finishTabByUrl(context: Context, website: Website, activityNames: List<String>?): Boolean {
        return findTaskAndExecuteAction(context, website, activityNames) { task ->
            Timber.d("Finishing task $website")
            task.finishAndRemoveTask()
        }
    }

    /**
     * Talks to activity manager and finds all active task. Then find a task that matches the input
     * criteria which is base url and optionally and preferred activity name the url belongs to.
     * Upon finding the task, executes {@param foundAction}
     */
    private fun findTaskAndExecuteAction(
            context: Context,
            website: Website,
            activityNames: List<String>?,
            foundAction: (task: ActivityManager.AppTask) -> Unit
    ): Boolean {
        try {
            val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            if (Utils.isLollipopAbove()) {
                val appTasks = am.appTasks
                Timber.d("No of app tasks ${appTasks?.size}")
                for (task in appTasks) {
                    val info = DocumentUtils.getTaskInfoFromTask(task)
                    info?.let {
                        try {
                            val intent = info.baseIntent
                            val url = intent.dataString
                            val componentClassName = intent.component!!.className

                            val urlMatches = url != null && website.matches(url)

                            val taskComponentMatches = activityNames?.contains(componentClassName)
                                    ?: TabsManager.ALL_BROWSING_ACTIVITIES.contains(componentClassName)

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

    override fun minimizeTabByUrl(url: String, fromClass: String, incognito: Boolean) {
        rxEventBus.post(TabsManager.MinimizeEvent(TabsManager.Tab(url, getTabType(fromClass))))
        if (preferences.webHeads() || rxPreferences.nativeBubbles.get() || preferences.minimizeToWebHead()) {
            // When minimizing, don't try to handle aggressive loading cases.
            openWebHeads(application, website = Website(url), fromMinimize = true, incognito = incognito)
        }
    }

    override fun processIncomingIntent(
            activity: Activity,
            intent: Intent
    ): Completable = io.reactivex.Single
            .fromCallable<Pair<String, Boolean>> {
                // Safety check against malicious intents
                val safeIntent = SafeIntent(intent)
                val url = safeIntent.dataString
                var proceed = true
                // The first thing to check is if we should blacklist.
                if (preferences.perAppSettings()) {
                    val lastApp = appDetectionManager.nonFilteredPackage
                    if (lastApp.isNotEmpty()) {
                        if (appRepository.isPackageBlacklisted(lastApp)) {
                            doBlacklistAction(activity, safeIntent)
                            proceed = false
                        } else if (appRepository.isPackageIncognito(lastApp)) {
                            doIncognitoAction(activity, url)
                            proceed = false
                        }
                    }
                }
                url to proceed
            }.flatMapCompletable { (url, proceed) ->
                if (proceed) {
                    openUrlInternal(activity, Website(url), fromApp = false)
                } else {
                    Completable.complete()
                }
            }
            .doOnError { Timber.e(it, "Critical error when processing incoming intent") }
            .onErrorComplete()
            .compose(schedulerProvider.poolToUi<Any>())

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun openArticle(context: Context, website: Website, newTab: Boolean, incognito: Boolean) {
        if (!reOrderTabByUrl(context, website, listOf(ArticleActivity::class.java.name))) {
            val intent = Intent(context, ArticleActivity::class.java).apply {
                data = website.preferredUri()
                if (context !is Activity) {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
                if (newTab || Preferences.get(context).mergeTabs()) {
                    addFlags(FLAG_ACTIVITY_NEW_DOCUMENT)
                    addFlags(FLAG_ACTIVITY_MULTIPLE_TASK)
                }
                if (incognito) {
                    putExtra(EXTRA_KEY_INCOGNITO, true)
                }
                putExtra(EXTRA_KEY_TOOLBAR_COLOR, customizedWebsiteColor(website))
            }
            context.startActivity(intent)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun openBrowsingTab(
            context: Context,
            website: Website,
            smart: Boolean,
            fromNewTab: Boolean,
            activityNames: List<String>?,
            incognito: Boolean
    ) {
        val reordered = smart && reOrderTabByUrl(context, website, activityNames)

        if (!reordered) {
            val isIncognito = preferences.fullIncognitoMode() || incognito

            val browsingActivity = if (shouldUseWebView(incognito)) {
                Intent(context, WebViewActivity::class.java)
            } else {
                Intent(context, CustomTabActivity::class.java)
            }.apply {
                data = website.preferredUri()
                putExtra(EXTRA_KEY_WEBSITE, website)
                putExtra(EXTRA_KEY_TOOLBAR_COLOR, customizedWebsiteColor(website))
                if (isIncognito) {
                    putExtra(EXTRA_KEY_INCOGNITO, true)
                }
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

    override fun shouldUseWebView(incognito: Boolean): Boolean {
        val canSafelyOpenCCT = !preferences.useWebView() && CustomTabs.getCustomTabSupportingPackages(application).isNotEmpty()
        val isIncognito = preferences.fullIncognitoMode() || incognito
        return !(!isIncognito && canSafelyOpenCCT)
    }

    override fun openWebHeads(
            context: Context,
            website: Website,
            fromMinimize: Boolean,
            fromAmp: Boolean,
            incognito: Boolean
    ) {
        val url = website.preferredUrl()
        val bubbles = rxPreferences.nativeBubbles.get()
        val webHeads = preferences.webHeads()

        floatingBubbleFactory[if (bubbles) NATIVE else WEB_HEADS].openBubble(
                website,
                fromMinimize,
                fromAmp,
                incognito,
                context,
                customizedWebsiteColor(website)
        )

        val shouldUseWebView = shouldUseWebView(incognito)

        // If this command was not issued for minimizing, then attempt aggressive loading.
        if (preferences.aggressiveLoading() && !fromMinimize) {
            when {
                preferences.articleMode() -> backgroundLoadingStrategyFactory[ARTICLE].perform(url)
                else -> {
                    if (shouldUseWebView) {
                        if (!bubbles) {
                            backgroundLoadingStrategyFactory[WEB_VIEW].perform(url)
                        }
                    } else {
                        backgroundLoadingStrategyFactory[CUSTOM_TAB].perform(url)
                    }
                    openBrowsingTab(
                            context,
                            website,
                            smart = true,
                            fromNewTab = false,
                            incognito = incognito
                    )
                }
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


    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getActiveTabs(): Single<List<TabsManager.Tab>> {
        return Single.create { emitter ->
            try {
                val am = application.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                emitter.onSuccess((am.appTasks ?: emptyList<ActivityManager.AppTask>())
                        .asSequence()
                        .map(DocumentUtils::getTaskInfoFromTask)
                        .filter { it != null && it.baseIntent?.dataString != null && it.baseIntent.component != null }
                        .map {
                            val url = it.baseIntent.dataString!!
                            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                            val type = getTabType(it.baseIntent.component!!.className)
                            TabsManager.Tab(url, type)
                        }.filter { it.type != OTHER }
                        .toMutableList())
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun closeAllTabs(): Single<List<TabsManager.Tab>> {
        return getActiveTabs()
                .toObservable()
                .flatMapIterable { it }
                .map {
                    finishTabByUrl(application, Website(it.url), listOf(it.getTargetActivityName()))
                    it
                }.toList()
                .toSingle()
    }

    /**
     * Get customized toolbar color based on user preferences
     */
    @ColorInt
    private fun customizedWebsiteColor(website: Website): Int {
        if (preferences.isColoredToolbar) {
            if (preferences.dynamicToolbar()) {
                var appColor = NO_COLOR
                var websiteColor = NO_COLOR

                if (preferences.dynamicToolbarOnApp()) {
                    val lastApp = appDetectionManager.filteredPackage
                    if (lastApp.isEmpty()) {
                        ServiceManager.startAppDetectionService(application)
                    } else {
                        appColor = appRepository.getPackageColorSync(lastApp)
                    }
                }
                if (preferences.dynamicToolbarOnWeb()) {
                    websiteColor = websiteRepository.getWebsiteColorSync(website.url)
                    if (websiteColor == NO_COLOR) {
                        websiteColor = website.themeColor()
                    }
                }
                return when {
                    appColor != NO_COLOR -> appColor
                    websiteColor != NO_COLOR -> websiteColor
                    else -> preferences.toolbarColor()
                }
            } else {
                return preferences.toolbarColor()
            }
        } else return ContextCompat.getColor(application, R.color.primary)
    }


    private fun doIncognitoAction(activity: Activity, url: String) {
        openUrl(activity, Website(url), fromApp = false, incognito = true)
        if (BuildConfig.DEBUG) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(activity, "Incognito", Toast.LENGTH_SHORT).show()
            }
        }
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
        Handler(Looper.getMainLooper()).post {
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
                    .dismissListener { activity.finish() }.show()
        }
    }
}