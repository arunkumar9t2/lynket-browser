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

package arun.com.chromer.di.activity

import android.app.Activity
import arun.com.chromer.browsing.amp.AmpResolverActivity
import arun.com.chromer.browsing.article.ArticleActivity
import arun.com.chromer.browsing.browserintercept.BrowserInterceptActivity
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.browsing.customtabs.CustomTabs
import arun.com.chromer.browsing.newtab.NewTabDialogActivity
import arun.com.chromer.browsing.optionspopup.ChromerOptionsActivity
import arun.com.chromer.browsing.providerselection.ProviderSelectionActivity
import arun.com.chromer.browsing.shareintercept.ShareInterceptActivity
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.bubbles.webheads.ui.context.WebHeadContextActivity
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.di.view.ViewComponent
import arun.com.chromer.history.HistoryActivity
import arun.com.chromer.home.HomeActivity
import arun.com.chromer.intro.ChromerIntroActivity
import arun.com.chromer.perapp.PerAppSettingsActivity
import arun.com.chromer.settings.browsingmode.BrowsingModeActivity
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity
import arun.com.chromer.shortcuts.HomeScreenShortcutCreatorActivity
import arun.com.chromer.tabs.ui.TabsActivity
import arun.com.chromer.tips.TipsActivity
import dagger.BindsInstance
import dagger.Subcomponent
import dev.arunkumar.android.dagger.activity.PerActivity

@PerActivity
@Subcomponent(modules = [(ActivityModule::class)])
interface ActivityComponent {
  fun customTabs(): CustomTabs

  fun fragmentComponentFactory(): FragmentComponent.Factory

  fun viewComponentFactory(): ViewComponent.Factory

  fun inject(perAppSettingsActivity: PerAppSettingsActivity)

  fun inject(homeActivity: HomeActivity)

  fun inject(browserInterceptActivity: BrowserInterceptActivity)

  fun inject(customTabActivity: CustomTabActivity)

  fun inject(activityComponent: ActivityComponent)

  fun inject(homeScreenShortcutCreatorActivity: HomeScreenShortcutCreatorActivity)

  fun inject(articleActivity: ArticleActivity)

  fun inject(browsingOptionsActivity: BrowsingOptionsActivity)

  fun inject(newTabDialogActivity: NewTabDialogActivity)

  fun inject(webHeadContextActivity: WebHeadContextActivity)

  fun inject(shareInterceptActivity: ShareInterceptActivity)

  fun inject(webViewActivity: WebViewActivity)

  fun inject(ampResolverActivity: AmpResolverActivity)

  fun inject(chromerOptionsActivity: ChromerOptionsActivity)

  fun inject(providerSelectionActivity: ProviderSelectionActivity)

  fun inject(chromerIntroActivity: ChromerIntroActivity)

  fun inject(tipsActivity: TipsActivity)

  fun inject(tabsActivity: TabsActivity)

  fun inject(historyActivity: HistoryActivity)

  fun inject(browsingModeActivity: BrowsingModeActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: Activity): ActivityComponent
  }
}
