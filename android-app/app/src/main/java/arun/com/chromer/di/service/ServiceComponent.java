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

package arun.com.chromer.di.service;

import org.jetbrains.annotations.NotNull;

import arun.com.chromer.appdetect.AppDetectService;
import arun.com.chromer.di.scopes.PerService;
import arun.com.chromer.shared.base.PreferenceQuickSettingsTile;
import arun.com.chromer.webheads.WebHeadService;
import dagger.Subcomponent;

@PerService
@Subcomponent(modules = {
        ServiceModule.class
})
public interface ServiceComponent {

    void inject(AppDetectService appDetectService);

    void inject(WebHeadService webHeadService);

    void inject(@NotNull PreferenceQuickSettingsTile preferenceQuickSettingsTile);
}
