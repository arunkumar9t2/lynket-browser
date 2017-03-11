package arun.com.chromer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;

import com.arun.rxgoogleinstant.RxSuggestions;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import arun.com.chromer.activities.SnackHelper;
import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.search.SuggestionItem;
import arun.com.chromer.util.RxUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

interface Home {
    interface View extends SnackHelper {
        void setSuggestions(@NonNull List<SuggestionItem> suggestions);

        void setRecents(@NonNull List<WebSite> webSites);
    }

    class Presenter {
        WeakReference<Home.View> viewRef;
        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        Presenter(@NonNull Home.View view) {
            viewRef = new WeakReference<>(view);
        }

        boolean isViewAttached() {
            return viewRef != null && viewRef.get() != null;
        }

        Home.View getView() {
            return viewRef.get();
        }

        void registerSearch(@NonNull EditText editText) {
            compositeSubscription.add(RxTextView.afterTextChangeEvents(editText)
                    .map(changeEvent -> changeEvent.editable().toString())
                    .filter(s -> !TextUtils.isEmpty(s)).subscribeOn(AndroidSchedulers.mainThread())
                    .onBackpressureLatest()
                    .debounce(300, TimeUnit.MILLISECONDS)
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
            compositeSubscription.add(HistoryRepository.getInstance(context)
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

        void cleanUp() {
            if (viewRef != null) {
                viewRef.clear();
                viewRef = null;
            }
            compositeSubscription.clear();
        }
    }
}
