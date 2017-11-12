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

package arun.com.chromer.activities.common;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import rx.subscriptions.CompositeSubscription;

public interface Base {
    abstract class Presenter<V extends MvpView> extends MvpBasePresenter<V> {
        protected final CompositeSubscription subs = new CompositeSubscription();

        public void onDestroy() {
            subs.clear();
            detachView(false);
        }

        public abstract void onResume();

        public abstract void onPause();
    }

    interface View extends MvpView {

    }
}
