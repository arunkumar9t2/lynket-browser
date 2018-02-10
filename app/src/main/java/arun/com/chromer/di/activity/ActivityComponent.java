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

package arun.com.chromer.di.activity;

import android.support.annotation.NonNull;

import arun.com.chromer.perapp.PerAppSettingsActivity;
import arun.com.chromer.browsing.amp.AmpResolverActivity;
import arun.com.chromer.browsing.article.ArticleActivity;
import arun.com.chromer.browsing.browserintercept.BrowserInterceptActivity;
import arun.com.chromer.browsing.customtabs.CustomTabActivity;
import arun.com.chromer.browsing.customtabs.CustomTabs;
import arun.com.chromer.browsing.newtab.NewTabDialogActivity;
import arun.com.chromer.browsing.optionspopup.ChromerOptionsActivity;
import arun.com.chromer.browsing.shareintercept.ShareInterceptActivity;
import arun.com.chromer.browsing.webview.WebViewActivity;
import arun.com.chromer.di.fragment.FragmentComponent;
import arun.com.chromer.di.fragment.FragmentModule;
import arun.com.chromer.di.scopes.PerActivity;
import arun.com.chromer.di.view.ViewComponent;
import arun.com.chromer.di.view.ViewModule;
import arun.com.chromer.home.HomeActivity;
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity;
import arun.com.chromer.shortcuts.HomeScreenShortcutCreatorActivity;
import arun.com.chromer.webheads.ui.context.WebHeadContextActivity;
import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = {
        ActivityModule.class
})
public interface ActivityComponent {
    @NonNull
    CustomTabs customTabs();

    @NonNull
    FragmentComponent newFragmentComponent(@NonNull FragmentModule fragmentModule);

    @NonNull
    ViewComponent newViewComponent(ViewModule viewModuleF);

    void inject(PerAppSettingsActivity perAppSettingsActivity);

    void inject(HomeActivity homeActivity);

    void inject(BrowserInterceptActivity browserInterceptActivity);

    void inject(CustomTabActivity customTabActivity);

    void inject(ActivityComponent activityComponent);

    void inject(HomeScreenShortcutCreatorActivity homeScreenShortcutCreatorActivity);

    void inject(ArticleActivity articleActivity);

    void inject(BrowsingOptionsActivity browsingOptionsActivity);

    void inject(NewTabDialogActivity newTabDialogActivity);

    void inject(WebHeadContextActivity webHeadContextActivity);

    void inject(ShareInterceptActivity shareInterceptActivity);

    void inject(WebViewActivity webViewActivity);

    void inject(AmpResolverActivity ampResolverActivity);

    void inject(ChromerOptionsActivity chromerOptionsActivity);
}
