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

package arun.com.chromer.di.app

import android.app.Application
import arun.com.chromer.Lynket
import arun.com.chromer.appdetect.AppDetectionManager
import arun.com.chromer.browsing.customtabs.bottombar.BottomBarReceiver
import arun.com.chromer.browsing.customtabs.callbacks.MinimizeBroadcastReceiver
import arun.com.chromer.browsing.customtabs.dynamictoolbar.AppColorExtractorJob
import arun.com.chromer.data.DataModule
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.di.service.ServiceComponent
import arun.com.chromer.home.HomeActivity
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsModule
import arun.com.chromer.util.drawer.GlideDrawerImageLoader
import dagger.BindsInstance
import dagger.Component
import dev.arunkumar.android.AppSchedulersModule
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    AppSchedulersModule::class,

    AppModule::class,

    HomeActivity.HomeBuilder::class,
    TabsModule::class,
    DataModule::class
  ]
)
interface AppComponent {

  fun glideDrawerImageLoader(): GlideDrawerImageLoader

  fun activityComponentFactory(): ActivityComponent.Factory

  fun serviceComponentFactory(): ServiceComponent.Factory

  fun appDetectionManager(): AppDetectionManager

  fun defaultTabsManager(): DefaultTabsManager

  fun inject(appColorExtractorJob: AppColorExtractorJob)

  fun inject(bottomBarReceiver: BottomBarReceiver)

  fun inject(minimizeBroadcastReceiver: MinimizeBroadcastReceiver)

  @Component.Factory
  interface Factory {
    fun create(@BindsInstance application: Application): AppComponent
  }
}

fun Application.appComponent() = (this as Lynket).appComponent
