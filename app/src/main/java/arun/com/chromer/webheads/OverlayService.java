package arun.com.chromer.webheads;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import timber.log.Timber;

/**
 * Created by Arunkumar on 24-02-2017.
 */
public abstract class OverlayService extends Service {
    @Nullable
    @Override
    public abstract IBinder onBind(Intent intent);

    @IntRange(from = 1, to = Long.MAX_VALUE)
    abstract int getNotificationId();

    @NonNull
    abstract Notification getNotification();

    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Timber.d("Exited overlay service since overlay permission was revoked");
                stopSelf();
                return;
            }
        }
        startForeground(getNotificationId(), getNotification());
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    protected void updateNotification(@NonNull Notification notification) {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(getNotificationId(), notification);
    }
}
