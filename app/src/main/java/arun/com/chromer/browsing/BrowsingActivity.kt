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

package arun.com.chromer.browsing

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import arun.com.chromer.R
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.SchedulerProvider
import arun.com.chromer.util.Utils
import arun.com.chromer.util.glide.GlideApp
import rx.Observable
import timber.log.Timber
import javax.inject.Inject

/**
 * Class definition for activity that shows a webpage.
 */
abstract class BrowsingActivity : BaseActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var rxEventBus: RxEventBus

    private lateinit var browsingViewModel: BrowsingViewModel

    protected var website: Website? = null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.data == null) {
            Toast.makeText(this, getString(R.string.unsupported_link), LENGTH_SHORT).show()
            finish()
            return
        }

        browsingViewModel = ViewModelProviders.of(this, viewModelFactory).get(BrowsingViewModel::class.java)

        loadWebsiteMetadata()
        setupMinimize()
    }

    private fun setupMinimize() {
        subs.add(rxEventBus
                .filteredEvents(TabsManager.MinimizeEvent::class.java)
                .filter { it.url.equals(intent?.dataString, ignoreCase = true) }
                .subscribe {
                    if (Utils.ANDROID_LOLLIPOP) {
                        moveTaskToBack(true)
                    }
                })
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun loadWebsiteMetadata() {
        val web = intent.getParcelableExtra<Website>(Constants.EXTRA_KEY_WEBSITE)
        website = if (web == null) {
            Website(intent.dataString)
        } else {
            if (Utils.ANDROID_LOLLIPOP) {
                setTaskDescriptionFromWebsite(web)
            }
            web
        }
        browsingViewModel.loadWebSiteDetails(intent.dataString)
                .filter { it is Result.Success && it.data != null }
                .map { (it as Result.Success).data!! }
                .subscribe {
                    // Save the updated website
                    website = it
                    // Set task description
                    if (Utils.ANDROID_LOLLIPOP) {
                        val (title, themeColor) = setTaskDescriptionFromWebsite(it)
                        setTaskDescriptionIcon(it, title, themeColor)
                    }
                    // Send to subclasses
                    onWebsiteLoaded(it)
                }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTaskDescriptionFromWebsite(website: Website): Pair<String, Int> {
        val title = website.safeLabel()
        val themeColor = if (website.themeColor() == Constants.NO_COLOR) ContextCompat.getColor(this, R.color.colorPrimary)
        else website.themeColor()
        // Set data without icon.
        setTaskDescription(ActivityManager.TaskDescription(title, null, themeColor))
        return Pair(title, themeColor)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTaskDescriptionIcon(website: Website, title: String, themeColor: Int) {
        subs.add(Observable.just(website)
                .map {
                    val icon = GlideApp.with(this).asBitmap().load(website).submit().get()
                    val palette = Palette.from(icon).generate()
                    val color = ColorUtil.getBestColorFromPalette(palette)
                    ActivityManager.TaskDescription(title, icon, if (color == Constants.NO_COLOR) themeColor else color)
                }
                .compose(SchedulerProvider.applyIoSchedulers())
                .doOnNext {
                    setTaskDescription(it)
                    onTaskColorSet(it.primaryColor)
                }
                .doOnError(Timber::e)
                .subscribe())
    }

    open fun onTaskColorSet(websiteThemeColor: Int) {
    }

    fun getCurrentUrl(): String {
        return intent.dataString
    }

    /**
     * Called when sufficient meta data about the current website is loaded.
     *
     * NOTE: This callback may be never called if loading fails.
     */
    abstract fun onWebsiteLoaded(website: Website)
}
