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

package arun.com.chromer.di.fragment

import arun.com.chromer.di.scopes.PerFragment
import arun.com.chromer.history.HistoryFragment
import arun.com.chromer.home.fragment.HomeFragment
import arun.com.chromer.intro.fragments.AppIntroFragment
import arun.com.chromer.intro.fragments.ArticleIntroFragment
import arun.com.chromer.intro.fragments.SlideOverExplanationFragment
import arun.com.chromer.intro.fragments.WebHeadsIntroFragment
import arun.com.chromer.tabs.ui.TabsFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [(FragmentModule::class)])
interface FragmentComponent {

    fun inject(homeFragment: HistoryFragment)

    fun inject(homeFragment: HomeFragment)

    fun inject(tabsFragment: TabsFragment)

    fun inject(appIntroFragment: AppIntroFragment)

    fun inject(slideOverExplanationFragment: SlideOverExplanationFragment)

    fun inject(webHeadsIntroFragment: WebHeadsIntroFragment)

    fun inject(articleIntroFragment: ArticleIntroFragment)
}
