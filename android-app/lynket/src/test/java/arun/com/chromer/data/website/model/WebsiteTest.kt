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

package arun.com.chromer.data.website.model

import arun.com.chromer.LynketRobolectricSuite
import org.junit.Assert.assertEquals
import org.junit.Test

class WebsiteTest : LynketRobolectricSuite() {

  @Test
  fun ampify() {
    val website = Website().apply {
      ampUrl = "http://www.yahoo.com"
      url = "http://www.google.com"
      canonicalUrl = "http://www.google.com"
    }
    val ampified = Website.Ampify(website)

    assertEquals(website.ampUrl, ampified.ampUrl)
    assertEquals(website.ampUrl, ampified.url)
    assertEquals(website.ampUrl, ampified.canonicalUrl)
  }
}
