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

package arun.com.chromer.di.modules;

import android.app.Application;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.apps.AppDiskStore;
import arun.com.chromer.data.apps.AppRepository;
import arun.com.chromer.data.apps.AppStore;
import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {

    Application application;

    public DataModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Preferences providesPreferences() {
        return Preferences.get(application);
    }

    @Provides
    @Singleton
    AppStore appStore() {
        return new AppDiskStore(application);
    }

    @Provides
    @Singleton
    AppRepository appRepository(@NonNull AppStore appStore) {
        return new AppRepository(application, appStore);
    }
}
