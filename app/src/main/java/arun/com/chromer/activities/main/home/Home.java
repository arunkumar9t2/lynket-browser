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

package arun.com.chromer.activities.main.home;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.activities.common.Base;
import arun.com.chromer.activities.common.Snackable;
import arun.com.chromer.data.history.BaseHistoryRepository;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.di.PerFragment;
import arun.com.chromer.search.SuggestionItem;
import arun.com.chromer.util.RxUtils;
import in.arunkumarsampath.suggestions.RxSuggestions;
import rx.Observable;
import timber.log.Timber;

interface Home {
    interface View extends Snackable, Base.View {
        void setSuggestions(@NonNull List<SuggestionItem> suggestions);

        void setRecents(@NonNull List<WebSite> webSites);
    }

    @PerFragment
    class Presenter extends Base.Presenter<View> {

        private final BaseHistoryRepository historyRepository;

        @Inject
        public Presenter(BaseHistoryRepository historyRepository) {
            this.historyRepository = historyRepository;
        }

        void registerSearch(@NonNull Observable<String> stringObservable) {
            subs.add(stringObservable
                    .compose(RxSuggestions.suggestionsTransformer())
                    .map(strings -> {
                        final List<SuggestionItem> suggestionItems = new ArrayList<>();
                        for (String string : strings) {
                            suggestionItems.add(new SuggestionItem(string, SuggestionItem.GOOGLE));
                        }
                        return suggestionItems;
                    }).doOnNext(suggestionItems -> {
                        if (isViewAttached()) {
                            getView().setSuggestions(suggestionItems);
                        }
                    }).doOnError(Timber::e)
                    .subscribe());
        }

        void loadRecents() {
            subs.add(historyRepository.recents()
                    .compose(RxUtils.applySchedulers())
                    .doOnError(Timber::e)
                    .doOnNext(webSites -> {
                        if (isViewAttached()) {
                            getView().setRecents(webSites);
                        }
                    })
                    .subscribe());
        }

        @Override
        public void onResume() {

        }

        @Override
        public void onPause() {

        }
    }
}
