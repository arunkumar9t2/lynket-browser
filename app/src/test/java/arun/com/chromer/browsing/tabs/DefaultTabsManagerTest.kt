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

package arun.com.chromer.browsing.tabs

import arun.com.chromer.ChromerRobolectricSuite
import org.junit.Before
import org.junit.Test
import javax.inject.Inject


/**
 * Created by Arunkumar on 15-12-2017.
 */
class DefaultTabsManagerTest : ChromerRobolectricSuite() {
    @Inject
    @JvmField
    var tabs: DefaultTabsManager? = null

    @Before
    fun setUp() {
        testAppComponent.inject(this)
    }

    @Test
    fun testInject() {
        assert(testAppComponent != null)
        assert(tabs != null)
    }
}