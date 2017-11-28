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

import arun.com.chromer.activities.CustomTabActivity;
import arun.com.chromer.activities.blacklist.BlacklistManagerActivity;
import arun.com.chromer.activities.browserintercept.BrowserInterceptActivity;
import arun.com.chromer.activities.main.MainActivity;
import arun.com.chromer.customtabs.CustomTabs;
import arun.com.chromer.di.PerActivity;
import arun.com.chromer.di.fragment.FragmentComponent;
import arun.com.chromer.di.fragment.FragmentModule;
import arun.com.chromer.shortcuts.HomeScreenShortcutCreatorActivity;
import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = {
        ActivityModule.class
})
public interface ActivityComponent {
    @NonNull
    FragmentComponent newFragmentComponent(@NonNull FragmentModule fragmentModule);

    @NonNull
    CustomTabs customTabs();

    void inject(BlacklistManagerActivity blacklistManagerActivity);

    void inject(MainActivity mainActivity);

    void inject(BrowserInterceptActivity browserInterceptActivity);

    void inject(CustomTabActivity customTabActivity);

    void inject(ActivityComponent activityComponent);

    void inject(@NonNull HomeScreenShortcutCreatorActivity homeScreenShortcutCreatorActivity);
}
