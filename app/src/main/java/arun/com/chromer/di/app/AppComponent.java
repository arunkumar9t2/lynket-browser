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

package arun.com.chromer.di.app;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import arun.com.chromer.appdetect.AppDetectionManager;
import arun.com.chromer.browsing.customtabs.bottombar.BottomBarReceiver;
import arun.com.chromer.browsing.customtabs.callbacks.MinimizeBroadcastReceiver;
import arun.com.chromer.browsing.customtabs.dynamictoolbar.AppColorExtractorJob;
import arun.com.chromer.browsing.tabs.DefaultTabsManager;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.di.activity.ActivityModule;
import arun.com.chromer.di.data.DataModule;
import arun.com.chromer.di.service.ServiceComponent;
import arun.com.chromer.di.service.ServiceModule;
import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        DataModule.class,
})
public interface AppComponent {
    @NonNull
    ActivityComponent newActivityComponent(@NonNull ActivityModule activityModule);

    ServiceComponent newServiceComponent(ServiceModule serviceModule);

    @NonNull
    AppDetectionManager appDetectionManager();

    DefaultTabsManager defaultTabsManager();

    void inject(AppColorExtractorJob appColorExtractorJob);

    void inject(BottomBarReceiver bottomBarReceiver);

    void inject(MinimizeBroadcastReceiver minimizeBroadcastReceiver);
}