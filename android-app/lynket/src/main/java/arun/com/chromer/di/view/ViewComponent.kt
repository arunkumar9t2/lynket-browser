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

package arun.com.chromer.di.view

import android.view.View
import arun.com.chromer.search.view.MaterialSearchView
import dagger.BindsInstance
import dagger.Subcomponent
import dev.arunkumar.android.dagger.view.PerView

@PerView
@Subcomponent(modules = [ViewModule::class])
interface ViewComponent {
  fun inject(materialSearchView: MaterialSearchView)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance view: View): ViewComponent
  }
}
