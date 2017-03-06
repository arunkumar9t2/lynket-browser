package arun.com.chromer.activities.history;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import arun.com.chromer.R;
import arun.com.chromer.activities.SnackHelper;
import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.RxUtils;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by arunk on 06-03-2017.
 */
interface History {
    interface View extends SnackHelper {
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
                    .compose(RxUtils.applySchedulers())
                    .doOnSubscribe(() -> {
                        if (isViewAttached()) {
                            getView().loading(true);
                        }
                    })
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
            compositeSubscription.add(HistoryRepository.getInstance(context)
                    .deleteAll()
                    .compose(RxUtils.applySchedulers())
                    .doOnError(Timber::e)
                    .doOnNext(rows -> loadHistory(context))
                    .doOnNext(rows -> {
                        if (isViewAttached()) {
                            getView().snack(String.format(context.getString(R.string.deleted_items), rows));
                        }
                    }).subscribe());
        }

        void deleteHistory(@NonNull Context context, @Nullable WebSite webSite) {
            if (webSite != null && webSite.url != null) {
                compositeSubscription.add(HistoryRepository.getInstance(context)
                        .delete(webSite)
                        .compose(RxUtils.applySchedulers())
                        .doOnError(Timber::e)
                        .doOnNext(result -> loadHistory(context))
                        .subscribe());
            }
        }
    }
}
