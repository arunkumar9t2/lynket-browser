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

package arun.com.chromer.di.view;

import androidx.annotation.NonNull;

import arun.com.chromer.di.scopes.PerView;
import arun.com.chromer.search.view.MaterialSearchView;
import dagger.Subcomponent;

@PerView
@Subcomponent(modules = {
        ViewModule.class
})
public interface ViewComponent {
    void inject(@NonNull MaterialSearchView materialSearchView);
}
