/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer;

import arun.com.chromer.di.app.AppComponent;
import arun.com.chromer.di.app.DaggerTestAppComponent;
import arun.com.chromer.di.app.TestAppComponent;
import arun.com.chromer.di.app.TestAppModule;
import arun.com.chromer.di.data.TestDataModule;

public class ChromerTestApplication extends Chromer {
    private TestAppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void initFabric() {
    }

    @Override
    public AppComponent getAppComponent() {
        if (appComponent == null) {
            appComponent = DaggerTestAppComponent.builder()
                    .testAppModule(new TestAppModule(this))
                    .testDataModule(new TestDataModule(this))
                    .build();
        }
        return appComponent;
    }
}