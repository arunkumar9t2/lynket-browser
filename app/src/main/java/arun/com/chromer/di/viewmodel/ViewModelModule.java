/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

package arun.com.chromer.di.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import arun.com.chromer.browsing.BrowsingViewModel;
import arun.com.chromer.browsing.article.BrowsingArticleViewModel;
import arun.com.chromer.history.HistoryFragmentViewModel;
import arun.com.chromer.home.fragment.HomeFragmentViewModel;
import arun.com.chromer.perapp.PerAppSettingViewModel;
import arun.com.chromer.tabs.ui.TabsViewModel;
import arun.com.chromer.util.viemodel.ViewModelFactory;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {
    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

    @Binds
    @IntoMap
    @ViewModelKey(BrowsingViewModel.class)
    abstract ViewModel bindBrowsingViewModel(BrowsingViewModel browsingViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TabsViewModel.class)
    abstract ViewModel tabsViewModel(TabsViewModel tabsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HomeFragmentViewModel.class)
    abstract ViewModel homeFragmentViewModel(HomeFragmentViewModel homeFragmentViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HistoryFragmentViewModel.class)
    abstract ViewModel historyFragmentViewModel(HistoryFragmentViewModel historyFragmentViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BrowsingArticleViewModel.class)
    abstract ViewModel bindArticleBrowsingViewModel(BrowsingArticleViewModel browsingArticleViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PerAppSettingViewModel.class)
    abstract ViewModel perAppSettingViewModel(PerAppSettingViewModel perAppSettingViewModel);
}