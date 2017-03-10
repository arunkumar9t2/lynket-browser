package arun.com.chromer.webheads;

import android.app.Notification;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Created by Arunkumar on 24-02-2017.
 */
public interface WebHeads {
    interface View {
        void updateNotification(@NonNull Notification notification);

        void initializeTrashy();
    }

    class Presenter<V extends View> {
        V view;

        WeakReference<V> viewRef;

        Presenter(@NonNull V view) {
            viewRef = new WeakReference<>(view);
        }

        boolean isViewAttached() {
            return viewRef != null && viewRef.get() != null;
        }

        V getView() {
            return viewRef.get();
        }


        void cleanUp() {

        }
    }
}
