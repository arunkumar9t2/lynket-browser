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

import android.app.Application;

import javax.inject.Singleton;

import arun.com.chromer.di.viewmodel.ViewModelModule;
import arun.com.chromer.shared.AppDetectionManager;
import arun.com.chromer.util.RxEventBus;
import dagger.Module;
import dagger.Provides;

@Module(includes = ViewModelModule.class)
public class AppModule {

    Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return application;
    }

    @Provides
    @Singleton
    AppDetectionManager providesAppDetectionManager() {
        return new AppDetectionManager(application);
    }

    @Provides
    @Singleton
    RxEventBus rxEventBus() {
        return new RxEventBus();
    }
}