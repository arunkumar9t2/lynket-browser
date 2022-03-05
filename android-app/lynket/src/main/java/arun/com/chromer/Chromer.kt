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
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import arun.com.chromer.di.app.AppComponent
import arun.com.chromer.di.app.DaggerAppComponent
import arun.com.chromer.util.ServiceManager
import com.airbnb.epoxy.EpoxyController
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.uber.rxdogtag.RxDogTag
import io.fabric.sdk.android.Fabric
import io.paperdb.Paper
import timber.log.Timber

/**
 * Created by Arun on 06/01/2016.
 */
open class Chromer : Application() {

  open val appComponent: AppComponent by lazy {
    DaggerAppComponent.factory().create(this)
  }

  override fun onCreate() {
    super.onCreate()
    initFabric()
    Paper.init(this)

    if (BuildConfig.DEBUG) {
      RxDogTag.install()
      Timber.plant(Timber.DebugTree())
    } else {
      Timber.plant(CrashlyticsTree())
    }
    ServiceManager.takeCareOfServices(applicationContext)

    initMaterialDrawer()

    initEpoxy()
  }

  private fun initEpoxy() {
    EpoxyController.setGlobalDebugLoggingEnabled(true)
  }

  private fun initMaterialDrawer() {
    DrawerImageLoader.init(appComponent.glideDrawerImageLoader())
      .withHandleAllUris(true)
  }

  protected open fun initFabric() {
    val core = CrashlyticsCore.Builder()
      .disabled(BuildConfig.DEBUG)
      .build()
    val crashlytics = Crashlytics.Builder().core(core).build()
    Fabric.with(this, crashlytics)
  }

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

  private class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
        return
      }
      Crashlytics.logException(t)
    }
  }

  companion object {
    init {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
  }
}
