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

package arun.com.chromer.webheads;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import arun.com.chromer.R;
import arun.com.chromer.shared.common.BaseService;
import arun.com.chromer.util.Utils;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Arunkumar on 24-02-2017.
 */
public abstract class OverlayService extends BaseService {
    @Nullable
    @Override
    public abstract IBinder onBind(Intent intent);

    @IntRange(from = 1, to = Long.MAX_VALUE)
    abstract int getNotificationId();

    @NonNull
    abstract Notification getNotification();

    public void onCreate() {
        super.onCreate();
        checkForOverlayPermission();
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

    protected void checkForOverlayPermission() {
        if (!Utils.isOverlayGranted(this)) {
            Toast.makeText(this, getString(R.string.web_head_permission_toast), LENGTH_LONG).show();
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Timber.d("Exited overlay service since overlay permission was revoked");
            stopSelf();
        }
    }
}
