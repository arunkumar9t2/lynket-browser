/*
 *
 *  Lynket
 *
 *  Copyright (C) 2023 Arunkumar
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

package arun.com.chromer

import android.app.Application
import arun.com.chromer.di.ActivityLifecycleCallbackInjector
import com.deliveryhero.whetstone.Whetstone
import com.deliveryhero.whetstone.app.ApplicationComponent
import com.deliveryhero.whetstone.app.ApplicationComponentOwner
import com.deliveryhero.whetstone.app.ContributesAppInjector
import javax.inject.Inject

@ContributesAppInjector(generateAppComponent = true)
class Lynket : Application(), ApplicationComponentOwner {

  override val applicationComponent: ApplicationComponent by lazy {
    GeneratedApplicationComponent.create(this)
  }

  @Inject
  lateinit var activityLifecycleCallbackInjector: ActivityLifecycleCallbackInjector

  override fun onCreate() {
    Whetstone.inject(this)
    super.onCreate()
    registerActivityLifecycleCallbacks(activityLifecycleCallbackInjector)
  }
}
