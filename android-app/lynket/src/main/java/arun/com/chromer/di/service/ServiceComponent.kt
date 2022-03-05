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
package arun.com.chromer.di.service

import android.app.Service
import arun.com.chromer.appdetect.AppDetectService
import arun.com.chromer.bubbles.webheads.WebHeadService
import arun.com.chromer.shared.base.PreferenceQuickSettingsTile
import dagger.BindsInstance
import dagger.Subcomponent
import dev.arunkumar.android.dagger.service.PerService

@PerService
@Subcomponent(modules = [ServiceModule::class])
interface ServiceComponent {
  fun inject(appDetectService: AppDetectService)
  fun inject(webHeadService: WebHeadService)
  fun inject(preferenceQuickSettingsTile: PreferenceQuickSettingsTile)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance service: Service): ServiceComponent
  }
}
