package arun.com.chromer.activities;

import android.support.annotation.NonNull;

/**
 * Interface to allow fragments to easily show snack bars in their attached activity.
 */
public interface SnackHelper {
    void snack(@NonNull final String message);

    void snackLong(@NonNull final String message);
}
