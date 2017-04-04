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

package arun.com.chromer;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by Arunkumar on 05-04-2017.
 */
public interface Base {
    abstract class Presenter<V extends MvpView> extends MvpBasePresenter<V> {
        final CompositeSubscription compositeSubscription = new CompositeSubscription();

        void onDestroy() {
            compositeSubscription.clear();
        }

        void onResume() {
        }

        void onPause() {
        }
    }

    interface View extends MvpView {

    }
}
