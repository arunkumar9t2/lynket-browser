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

package arun.com.chromer.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Adapter to let us implement only what's needed from the interface.
 */
open class ActivityLifeCycleCallbackAdapter : Application.ActivityLifecycleCallbacks {
  override fun onActivityPaused(activity: Activity) {}

  override fun onActivityResumed(activity: Activity) {}

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityDestroyed(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
}
