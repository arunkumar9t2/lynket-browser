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

package arun.com.chromer

import android.app.Application
import arun.com.chromer.di.app.TestAppComponent
import arun.com.chromer.settings.Preferences
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [16, 21], constants = BuildConfig::class, application = ChromerTestApplication::class)
abstract class ChromerRobolectricSuite {
    lateinit var testAppComponent: TestAppComponent

    @Inject
    @JvmField
    var preferences: Preferences? = null

    val application: Application
        get() = RuntimeEnvironment.application

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        testAppComponent = (application as ChromerTestApplication).appComponent as TestAppComponent
        testAppComponent.inject(this)
    }

    internal fun clearPreferences() {
        preferences?.defaultSharedPreferences?.edit()?.clear()?.commit()
    }
}

