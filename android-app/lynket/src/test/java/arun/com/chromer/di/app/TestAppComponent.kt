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

import arun.com.chromer.LynketRobolectricSuite
import arun.com.chromer.data.apps.DefaultAppRepositoryTest
import arun.com.chromer.di.data.TestDataModule
import arun.com.chromer.home.HomeActivity
import arun.com.chromer.tabs.DefaultTabsManagerTest
import arun.com.chromer.tabs.TabsModule
import dagger.Component
import dev.arunkumar.android.TestSchedulersModule
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    TestSchedulersModule::class,
    TestAppModule::class,
    HomeActivity.HomeBuilder::class,
    TabsModule::class,
    TestDataModule::class
  ]
)
interface TestAppComponent : AppComponent {

  fun inject(chromerRobolectricSuite: LynketRobolectricSuite)

  fun inject(defaultTabsManagerTest: DefaultTabsManagerTest)

  fun inject(defaultAppRepositoryTest: DefaultAppRepositoryTest)
}
