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

package arun.com.chromer.extenstions

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import arun.com.chromer.util.ActivityLifeCycleCallbackAdapter

/**
 * Created by arunk on 05-02-2018.
 */

inline fun <T> LiveData<T>.watch(owner: LifecycleOwner, crossinline observer: (T) -> Unit) {
  this.observe(owner) { observer(it) }
}


inline fun <T> LiveData<T>.observeUntilOnDestroy(
  sourceActivity: Activity?,
  crossinline observer: (T) -> Unit
) {
  val valueObserver: Observer<T> = Observer {
    observer(it)
  }
  this.observeForever(valueObserver)
  sourceActivity?.application?.registerActivityLifecycleCallbacks(object :
    ActivityLifeCycleCallbackAdapter() {
    override fun onActivityDestroyed(activity: Activity) {
      if (activity == sourceActivity) {
        activity.application?.unregisterActivityLifecycleCallbacks(this)
        this@observeUntilOnDestroy.removeObserver(valueObserver)
      }
    }
  })
}
