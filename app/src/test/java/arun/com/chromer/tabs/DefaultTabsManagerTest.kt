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

import android.content.Intent
import arun.com.chromer.ChromerRobolectricSuite
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.home.HomeActivity
import arun.com.chromer.settings.Preferences
import arun.com.chromer.webheads.WebHeadService
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import javax.inject.Inject


/**
 * Created by Arunkumar on 15-12-2017.
 */
class DefaultTabsManagerTest : ChromerRobolectricSuite() {
    @Inject
    @JvmField
    var tabs: DefaultTabsManager? = null
    @Inject
    @JvmField
    var preferences: Preferences? = null

    private val url = "https://www.example.com"

    @Before
    fun setUp() {
        testAppComponent.inject(this)
    }

    @Test
    fun testInject() {
        assert(testAppComponent != null)
        assert(tabs != null)
        assert(preferences != null)
    }

    @Test
    fun testHomeActivityClearedOnExternalIntent() {
        clearPreferences()
        preferences?.mergeTabs(false)
        preferences?.webHeads(false)

        val homeActivity = Robolectric.buildActivity(HomeActivity::class.java).create().get()
        val homeActivityShadow = shadowOf(homeActivity)

        tabs?.openUrl(RuntimeEnvironment.application, Website(url), fromApp = false)
        assert(homeActivityShadow.isFinishing)
    }


    @Test
    fun testWebHeadsOpened() {
        clearPreferences()
        preferences?.webHeads(true)
        val shadowApp = Shadows.shadowOf(RuntimeEnvironment.application)

        tabs?.openUrl(RuntimeEnvironment.application, Website(url), fromApp = false, fromWebHeads = false)
        assert(shadowApp.peekNextStartedService().component == Intent(RuntimeEnvironment.application, WebHeadService::class.java).component)
        assert(shadowApp.nextStartedService.dataString == url)

        tabs?.openUrl(RuntimeEnvironment.application, Website(url), fromApp = false, fromWebHeads = true)
        assert(shadowApp.nextStartedService == null)
    }

    private fun clearPreferences() {
        preferences?.defaultSharedPreferences?.edit()?.clear()?.commit()
    }

}