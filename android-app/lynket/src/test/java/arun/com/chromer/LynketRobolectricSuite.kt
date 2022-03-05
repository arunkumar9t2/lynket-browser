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

package arun.com.chromer

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import arun.com.chromer.di.app.TestAppComponent
import arun.com.chromer.settings.Preferences
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import rx.plugins.RxJavaHooks
import rx.schedulers.Schedulers
import javax.inject.Inject


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [19, 21], application = LynketTestApplication::class)
abstract class LynketRobolectricSuite {
  lateinit var testAppComponent: TestAppComponent

  @Inject
  lateinit var preferences: Preferences

  val application: Application get() = ApplicationProvider.getApplicationContext()

  @Before
  fun setup() {
    setupRxSchedulers()
    testAppComponent = (application as LynketTestApplication).appComponent as TestAppComponent
    testAppComponent.inject(this)
  }


  private fun setupRxSchedulers() {
    RxJavaHooks.setOnComputationScheduler { Schedulers.trampoline() }
    RxJavaHooks.setOnIOScheduler { Schedulers.trampoline() }
    RxJavaHooks.setOnNewThreadScheduler { Schedulers.trampoline() }
  }

  internal fun clearPreferences() {
    preferences.defaultSharedPreferences.edit()?.clear()?.commit()
  }
}

