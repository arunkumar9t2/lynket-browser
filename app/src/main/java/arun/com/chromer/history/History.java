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

package arun.com.chromer.history;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.di.scopes.PerFragment;
import arun.com.chromer.shared.base.Base;
import arun.com.chromer.shared.base.Snackable;
import arun.com.chromer.util.SchedulerProvider;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by arunk on 06-03-2017.
 */
interface History {
    interface View extends Snackable, Base.View {
        void loading(boolean loading);

        void setCursor(@Nullable Cursor cursor);
    }

    @PerFragment
    class Presenter extends Base.Presenter<History.View> {

        private final HistoryRepository historyRepository;

        @Inject
        public Presenter(HistoryRepository historyRepository) {
            this.historyRepository = historyRepository;
        }

        void loadHistory() {
            if (isViewAttached()) {
                getView().loading(true);
            }
            subs.add(historyRepository.getAllItemsCursor()
                    .compose(SchedulerProvider.applyIoSchedulers())
                    .toSingle()
                    .doOnSuccess(cursor -> {
                        if (isViewAttached()) {
                            getView().loading(false);
                            getView().setCursor(cursor);
                        }
                    })
                    .doOnError(throwable -> {
                        // Show error
                    }).subscribe());
        }

        void deleteAll(@NonNull Context context) {
            subs.add(historyRepository
                    .deleteAll()
                    .compose(SchedulerProvider.applyIoSchedulers())
                    .doOnError(Timber::e)
                    .doOnNext(rows -> loadHistory())
                    .doOnNext(rows -> {
                        if (isViewAttached()) {
                            getView().snack(String.format(context.getString(R.string.deleted_items), rows));
                        }
                    }).subscribe());
        }

        void deleteHistory(@NonNull Observable<Website> webSiteObservable) {
            subs.add(webSiteObservable
                    .filter(webSite -> webSite != null && webSite.url != null)
                    .flatMap(historyRepository::delete)
                    .compose(SchedulerProvider.applyIoSchedulers())
                    .doOnError(Timber::e)
                    .doOnNext(result -> loadHistory())
                    .subscribe());
        }
    }
}
