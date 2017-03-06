package arun.com.chromer.activities.history;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.util.RxUtils;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by arunk on 06-03-2017.
 */

public interface History {
    interface View {
        void loading(boolean loading);

        void setCursor(@Nullable Cursor cursor);
    }

    class Presenter {
        WeakReference<History.View> viewRef;
        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        Presenter(@NonNull History.View view) {
            viewRef = new WeakReference<>(view);
        }

        boolean isViewAttached() {
            return viewRef != null && viewRef.get() != null;
        }

        History.View getView() {
            return viewRef.get();
        }

        void cleanUp() {
            if (viewRef != null) {
                viewRef.clear();
                viewRef = null;
            }
            compositeSubscription.clear();
        }

        void loadHistory(@NonNull Context context) {
            compositeSubscription.add(HistoryRepository.getInstance(context).getAllItemsCursor()
                    .doOnSubscribe(() -> {
                        if (isViewAttached()) {
                            getView().loading(true);
                        }
                    })
                    .compose(RxUtils.applySchedulers())
                    .doOnNext(cursor -> {
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

        }

    }
}
