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
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import arun.com.chromer.R
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.observeUntilActivityDestroyed
import arun.com.chromer.settings.Preferences
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.Utils
import javax.inject.Inject

/**
 * Class definition for activity that shows a webpage.
 */
abstract class BrowsingActivity : BaseActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var rxEventBus: RxEventBus
    @Inject
    lateinit var preferences: Preferences

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

        observeViewModel(savedInstanceState)
        setupMinimize()
    }

    fun getCurrentUrl(): String {
        return intent.dataString
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

    private fun observeViewModel(savedInstanceState: Bundle?) {
        browsingViewModel.apply {
            websiteLiveData.observeUntilActivityDestroyed(this@BrowsingActivity, {
                when (it) {
                    is Result.Success -> {
                        website = it.data!!
                        onWebsiteLoaded(website!!)
                    }
                }
            })

            toolbarColor.observeUntilActivityDestroyed(this@BrowsingActivity, { color ->
                onToolbarColorSet(color!!)
            })

            activityDescription.observeUntilActivityDestroyed(this@BrowsingActivity, { task ->
                if (Utils.ANDROID_LOLLIPOP) {
                    setTaskDescription(task)
                }
            })

            if (savedInstanceState == null) {
                val websiteResult = Result.Success<Website>(intent.getParcelableExtra(Constants.EXTRA_KEY_WEBSITE))
                websiteLiveData.value = websiteResult
                toolbarColor.value = intent.getIntExtra(Constants.EXTRA_KEY_TOOLBAR_COLOR, Constants.NO_COLOR)
            }
            loadWebSiteDetails(getCurrentUrl())
        }
    }

    protected fun loadWebsiteDetails(url: String) {
        browsingViewModel.loadWebSiteDetails(url)
    }

    /**
     * Called when meta data about the current website is loaded.
     *
     */
    abstract fun onWebsiteLoaded(website: Website)

    open fun onToolbarColorSet(websiteThemeColor: Int) {
    }
}
