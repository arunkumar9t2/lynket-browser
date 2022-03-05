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

package arun.com.chromer.di.viewmodel

import androidx.lifecycle.ViewModel
import arun.com.chromer.browsing.BrowsingViewModel
import arun.com.chromer.browsing.article.BrowsingArticleViewModel
import arun.com.chromer.browsing.providerselection.ProviderSelectionViewModel
import arun.com.chromer.history.HistoryFragmentViewModel
import arun.com.chromer.home.fragment.HomeFragmentViewModel
import arun.com.chromer.perapp.PerAppSettingsViewModel
import arun.com.chromer.tabs.ui.TabsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dev.arunkumar.android.dagger.viewmodel.ViewModelKey

@Module
abstract class ViewModelModule {
  @Binds
  @IntoMap
  @ViewModelKey(BrowsingViewModel::class)
  internal abstract fun BrowsingViewModel.bindBrowsingViewModel(): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(TabsViewModel::class)
  internal abstract fun TabsViewModel.tabsViewModel(): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(HomeFragmentViewModel::class)
  internal abstract fun HomeFragmentViewModel.homeFragmentViewModel(): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(HistoryFragmentViewModel::class)
  internal abstract fun HistoryFragmentViewModel.historyFragmentViewModel(): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(BrowsingArticleViewModel::class)
  internal abstract fun BrowsingArticleViewModel.bindArticleBrowsingViewModel(): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(PerAppSettingsViewModel::class)
  internal abstract fun PerAppSettingsViewModel.perAppSettingViewModel(): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ProviderSelectionViewModel::class)
  internal abstract fun ProviderSelectionViewModel.providerSelectionViewModel(): ViewModel
}
