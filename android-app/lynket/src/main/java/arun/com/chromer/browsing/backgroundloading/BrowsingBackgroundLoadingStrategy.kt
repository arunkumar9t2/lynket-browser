/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.browsing.backgroundloading

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.webview.EmbeddableWebViewActivity
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.util.ActivityLifeCycleCallbackAdapter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

typealias UnRegisterAction = () -> Unit

private fun Application.safeActivityStarted(action: (Activity, UnRegisterAction) -> Unit) {
  registerActivityLifecycleCallbacks(object : ActivityLifeCycleCallbackAdapter() {
    override fun onActivityStarted(activity: Activity) {
      super.onActivityStarted(activity)
      val unRegisterAction: UnRegisterAction = {
        Timber.i("Unregistering lifecycle callbacks")
        unregisterActivityLifecycleCallbacks(this)
      }
      activity.let { startedActivity ->
        try {
          action(startedActivity, unRegisterAction)
        } catch (e: Exception) {
          Timber.e(e)
          unRegisterAction()
        }
      }
    }
  })
}

abstract class BrowsingBackgroundLoadingStrategy(
  private val application: Application,
) : BackgroundLoadingStrategy {

  abstract val activityClasses: List<Class<out Activity>>

  override fun prepare(url: String) {
    application.safeActivityStarted { startedActivity, unRegisterAction ->
      if (activityClasses.any { it.isAssignableFrom(startedActivity.javaClass) }) {
        val activityUrl = startedActivity.intent?.dataString
        if (url == activityUrl) {
          Handler(Looper.getMainLooper()).postDelayed({
            startedActivity.moveTaskToBack(true)
            Timber.d("Moved $activityUrl to back")
            unRegisterAction()
          }, 1000)
        }
      } else {
        Timber.d("No match for $startedActivity")
      }
    }
  }
}

@Singleton
class WebViewBackgroundLoadingStrategy
@Inject
constructor(
  application: Application,
) : BrowsingBackgroundLoadingStrategy(application) {
  override val activityClasses: List<Class<out Activity>> = listOf(
    WebViewActivity::class.java,
    EmbeddableWebViewActivity::class.java
  )
}

@Singleton
class CustomTabBackgroundLoadingStrategy
@Inject
constructor(
  application: Application,
) : BrowsingBackgroundLoadingStrategy(application) {
  override val activityClasses: List<Class<out Activity>> = listOf(
    CustomTabActivity::class.java
  )
}
