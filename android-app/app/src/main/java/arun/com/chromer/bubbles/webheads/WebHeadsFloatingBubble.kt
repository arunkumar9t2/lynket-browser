package arun.com.chromer.bubbles.webheads

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.core.content.ContextCompat
import arun.com.chromer.bubbles.FloatingBubble
import arun.com.chromer.shared.Constants.*
import arun.com.chromer.util.Utils
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WebHeadsFloatingBubble
@Inject
constructor(private val application: Application) : FloatingBubble {

    override fun openBubble(
            url: String,
            fromMinimize: Boolean,
            fromAmp: Boolean,
            incognito: Boolean,
            context: Context?
    ) {
        (context ?: application).let { ctx ->
            if (Utils.isOverlayGranted(ctx)) {
                val webHeadLauncher = Intent(ctx, WebHeadService::class.java).apply {
                    data = Uri.parse(url)
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                    putExtra(EXTRA_KEY_MINIMIZE, fromMinimize)
                    putExtra(EXTRA_KEY_FROM_AMP, fromAmp)
                    putExtra(EXTRA_KEY_INCOGNITO, incognito)
                }
                ContextCompat.startForegroundService(ctx, webHeadLauncher)
            } else {
                Utils.openDrawOverlaySettings(ctx)
            }
        }
    }
}

