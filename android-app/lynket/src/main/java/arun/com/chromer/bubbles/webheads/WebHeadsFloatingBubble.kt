package arun.com.chromer.bubbles.webheads

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.content.ContextCompat
import arun.com.chromer.bubbles.FloatingBubble
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.shared.Constants.*
import arun.com.chromer.util.Utils
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WebHeadsFloatingBubble
@Inject
constructor(
  private val application: Application,
  private val schedulerProvider: SchedulerProvider
) : FloatingBubble {

  override fun openBubble(
    website: Website,
    fromMinimize: Boolean,
    fromAmp: Boolean,
    incognito: Boolean,
    context: Context?,
    color: Int
  ) {
    (context ?: application).let { ctx ->
      if (Utils.isOverlayGranted(ctx)) {
        val webHeadLauncher = Intent(ctx, WebHeadService::class.java).apply {
          data = website.preferredUri()
          addFlags(FLAG_ACTIVITY_NEW_TASK)
          putExtra(EXTRA_KEY_MINIMIZE, fromMinimize)
          putExtra(EXTRA_KEY_FROM_AMP, fromAmp)
          putExtra(EXTRA_KEY_INCOGNITO, incognito)
        }
        ContextCompat.startForegroundService(ctx, webHeadLauncher)
      } else {
        schedulerProvider.ui.scheduleDirect {
          Utils.openDrawOverlaySettings(ctx)
        }
      }
    }
  }
}

