/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.bubbles.webheads

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.IntRange
import arun.com.chromer.R
import arun.com.chromer.shared.base.service.BaseService
import arun.com.chromer.util.ServiceManager
import arun.com.chromer.util.Utils
import timber.log.Timber

/**
 * Created by Arunkumar on 24-02-2017.
 */
abstract class OverlayService : BaseService() {
  @IntRange(from = 1)
  abstract fun getNotificationId(): Int

  abstract fun getNotification(): Notification

  abstract override fun onBind(intent: Intent): IBinder?

  override fun onCreate() {
    super.onCreate()
    checkForOverlayPermission()
    startForeground(getNotificationId(), getNotification())
  }

  protected fun stopService() {
    Timber.d("Stopping service.")
    ServiceManager.restartAppDetectionService(this) // Temp hack for Oreo.
    stopForeground(true)
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(getNotificationId())
    stopSelf()
  }

  protected fun updateNotification(notification: Notification) {
    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.notify(getNotificationId(), notification)
  }

  protected fun checkForOverlayPermission() {
    if (!Utils.isOverlayGranted(this)) {
      Toast.makeText(this, getString(R.string.web_head_permission_toast), LENGTH_LONG).show()
      val intent =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      startActivity(intent)
      Timber.d("Exited overlay service since overlay permission was revoked")
      stopSelf()
    }
  }
}
