package arun.com.chromer.data.website.model

import arun.com.chromer.ChromerRobolectricSuite
import org.junit.Assert.assertEquals
import org.junit.Test

class WebsiteTest : ChromerRobolectricSuite() {

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