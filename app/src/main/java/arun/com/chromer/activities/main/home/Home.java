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

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;

import com.arun.rxsuggestions.RxSuggestions;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import arun.com.chromer.activities.Snackable;
import arun.com.chromer.activities.mvp.Base;
import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.search.SuggestionItem;
import arun.com.chromer.util.RxUtils;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

interface Home {
    interface View extends Snackable, Base.View {
        void setSuggestions(@NonNull List<SuggestionItem> suggestions);

        void setRecents(@NonNull List<WebSite> webSites);
    }

    class Presenter extends Base.Presenter<View> {

        void registerSearch(@NonNull EditText editText) {
            subs.add(RxTextView.afterTextChangeEvents(editText)
                    .map(changeEvent -> changeEvent.editable().toString())
                    .filter(s -> !TextUtils.isEmpty(s)).subscribeOn(AndroidSchedulers.mainThread())
                    .debounce(150, TimeUnit.MILLISECONDS)
                    .onBackpressureLatest()
                    .doOnNext(s -> Timber.d("Query: %s", s))
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

        void loadRecents(@NonNull Context context) {
            subs.add(HistoryRepository.getInstance(context)
                    .recents()
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
