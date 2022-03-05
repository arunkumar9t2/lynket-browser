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

package arun.com.chromer.data.apps

import arun.com.chromer.LynketRobolectricSuite
import arun.com.chromer.data.common.App
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/**
 * Created by arunk on 10-02-2018.
 */
class DefaultAppRepositoryTest : LynketRobolectricSuite() {
  @Inject
  @JvmField
  var appRepository: AppRepository? = null

  private val packageName = "PackageName"

  @Before
  fun setUp() {
    testAppComponent.inject(this)
  }

  @After
  fun tearDown() {
    appRepository = null
  }

  @Test
  fun allApps() {
    val testSubscriber = appRepository!!.allApps().test()

    testSubscriber.awaitTerminalEvent()
      .assertNoErrors()
      .assertCompleted()
      .assertReceivedOnNext(listOf(listOf(App("App", "Package", false, false, 0))))
  }

  @Test
  fun testBlacklistSet() {
    appRepository!!.setPackageBlacklisted(packageName).subscribe()
    assertTrue(appRepository!!.isPackageBlacklisted(packageName))
    assertFalse(appRepository!!.isPackageIncognito(packageName))
  }

  @Test
  fun testIncognitoSet() {
    appRepository!!.setPackageIncognito(packageName).subscribe()
    assertTrue(appRepository!!.isPackageIncognito(packageName))
    assertFalse(appRepository!!.isPackageBlacklisted(packageName))
  }
}
