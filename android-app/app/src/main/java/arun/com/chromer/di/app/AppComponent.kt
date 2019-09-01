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

package arun.com.chromer.di.app

import arun.com.chromer.appdetect.AppDetectionManager
import arun.com.chromer.browsing.customtabs.bottombar.BottomBarReceiver
import arun.com.chromer.browsing.customtabs.callbacks.MinimizeBroadcastReceiver
import arun.com.chromer.browsing.customtabs.dynamictoolbar.AppColorExtractorJob
import arun.com.chromer.data.DataModule
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.di.activity.ActivityModule
import arun.com.chromer.di.service.ServiceComponent
import arun.com.chromer.di.service.ServiceModule
import arun.com.chromer.home.HomeActivity
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsModule
import arun.com.chromer.util.drawer.GlideDrawerImageLoader
import arun.com.chromer.webheads.BubbleModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,

    HomeActivity.HomeBuilder::class,
    TabsModule::class,
    DataModule::class,
    BubbleModule::class
])
interface AppComponent {

    fun glideDrawerImageLoader(): GlideDrawerImageLoader

    fun newActivityComponent(activityModule: ActivityModule): ActivityComponent

    fun newServiceComponent(serviceModule: ServiceModule): ServiceComponent

    fun appDetectionManager(): AppDetectionManager

    fun defaultTabsManager(): DefaultTabsManager

    fun inject(appColorExtractorJob: AppColorExtractorJob)

    fun inject(bottomBarReceiver: BottomBarReceiver)

    fun inject(minimizeBroadcastReceiver: MinimizeBroadcastReceiver)
}