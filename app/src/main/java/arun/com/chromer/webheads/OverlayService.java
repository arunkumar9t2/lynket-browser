package arun.com.chromer.webheads;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Created by Arunkumar on 24-02-2017.
 */
public abstract class OverlayService extends Service {
    @Nullable
    @Override
    public abstract IBinder onBind(Intent intent);

    abstract int getNotificationId();

    abstract Notification getNotification();

    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
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
}
