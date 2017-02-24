package arun.com.chromer;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;

import com.arun.rxgoogleinstant.RxSuggestions;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import arun.com.chromer.search.SuggestionItem;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public interface Home {
    interface View {
        void setSuggestions(@NonNull List<SuggestionItem> suggestions);
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
                    .map(new Func1<TextViewAfterTextChangeEvent, String>() {
                        @Override
                        public String call(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
                            return textViewAfterTextChangeEvent.editable().toString();
                        }
                    }).filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            return !TextUtils.isEmpty(s);
                        }
                    }).subscribeOn(AndroidSchedulers.mainThread())
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .compose(RxSuggestions.suggestionsTransformer())
                    .map(new Func1<List<String>, List<SuggestionItem>>() {
                        @Override
                        public List<SuggestionItem> call(List<String> strings) {
                            final List<SuggestionItem> suggestionItems = new ArrayList<>();
                            for (String string : strings) {
                                suggestionItems.add(new SuggestionItem(string, SuggestionItem.GOOGLE));
                            }
                            return suggestionItems;
                        }
                    }).doOnNext(new Action1<List<SuggestionItem>>() {
                        @Override
                        public void call(List<SuggestionItem> suggestionItems) {
                            if (isViewAttached()) {
                                getView().setSuggestions(suggestionItems);
                            }
                        }
                    })
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Timber.e(throwable.toString());
                        }
                    }).subscribe());
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
