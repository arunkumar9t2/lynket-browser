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

package arun.com.chromer.activities.main;

import javax.inject.Inject;

import arun.com.chromer.activities.common.Base;
import arun.com.chromer.activities.common.Snackable;
import arun.com.chromer.di.PerActivity;

public interface MainScreen {
    interface View extends Base.View, Snackable {
    }

    @PerActivity
    class Presenter extends Base.Presenter<MainScreen.View> {

        @Inject
        public Presenter() {
        }

        @Override
        public void onResume() {

        }

        @Override
        public void onPause() {

        }
    }
}
