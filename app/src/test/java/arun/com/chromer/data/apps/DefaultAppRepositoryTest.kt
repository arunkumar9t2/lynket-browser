package arun.com.chromer.data.apps

import arun.com.chromer.ChromerRobolectricSuite
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
class DefaultAppRepositoryTest : ChromerRobolectricSuite() {
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