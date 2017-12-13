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

package arun.com.chromer.browsing.browserintercept;

import javax.inject.Inject;

import arun.com.chromer.di.scopes.PerActivity;
import arun.com.chromer.shared.base.Base;

/**
 * Created by arunk on 12-11-2017.
 */
public interface BrowserIntercept {

    interface View extends Base.View {

    }

    @PerActivity
    class Presenter extends Base.Presenter<BrowserIntercept.View> {
        @Inject
        public Presenter() {
        }
    }
}
