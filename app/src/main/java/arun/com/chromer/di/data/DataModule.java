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

package arun.com.chromer.di.data;

import android.app.Application;

import javax.inject.Singleton;

import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.apps.AppDiskStore;
import arun.com.chromer.data.apps.AppRepository;
import arun.com.chromer.data.apps.BaseAppRepository;
import arun.com.chromer.data.apps.store.AppStore;
import arun.com.chromer.data.history.BaseHistoryRepository;
import arun.com.chromer.data.history.HistoryDiskStore;
import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.data.history.HistoryStore;
import arun.com.chromer.data.qualifiers.Disk;
import arun.com.chromer.data.qualifiers.Network;
import arun.com.chromer.data.website.BaseWebsiteRepository;
import arun.com.chromer.data.website.WebsiteDiskStore;
import arun.com.chromer.data.website.WebsiteNetworkStore;
import arun.com.chromer.data.website.WebsiteRepository;
import arun.com.chromer.data.website.WebsiteStore;
import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {
    private Application application;

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
    AppStore appStore(AppDiskStore appDiskStore) {
        return appDiskStore;
    }

    @Provides
    @Singleton
    BaseAppRepository appRepository(AppRepository appRepository) {
        return appRepository;
    }

    @Provides
    @Singleton
    HistoryStore historyStore(HistoryDiskStore historyDiskStore) {
        return historyDiskStore;
    }

    @Provides
    @Singleton
    BaseHistoryRepository historyRepository(HistoryRepository historyRepository) {
        return historyRepository;
    }

    @Provides
    @Singleton
    @Disk
    WebsiteStore websiteDiskStore(WebsiteDiskStore websiteDiskStore) {
        return websiteDiskStore;
    }

    @Provides
    @Singleton
    @Network
    WebsiteStore websiteNetworkStore(WebsiteNetworkStore websiteNetworkStore) {
        return websiteNetworkStore;
    }

    @Provides
    @Singleton
    BaseWebsiteRepository websiteRepository(WebsiteRepository websiteRepository) {
        return websiteRepository;
    }

}
