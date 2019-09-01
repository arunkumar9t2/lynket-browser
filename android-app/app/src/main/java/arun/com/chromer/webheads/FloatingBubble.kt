package arun.com.chromer.webheads

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import arun.com.chromer.R
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.shared.Constants.*
import arun.com.chromer.util.Utils
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

interface FloatingBubble {

    fun openBubble(
            url: String,
            fromMinimize: Boolean,
            fromAmp: Boolean,
            incognito: Boolean,
            context: Context? = null
    )
}


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

        (context ?: application).let {
            if (Utils.isOverlayGranted(it)) {
                val webHeadLauncher = Intent(it, WebHeadService::class.java).apply {
                    data = Uri.parse(url)
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                    putExtra(EXTRA_KEY_MINIMIZE, fromMinimize)
                    putExtra(EXTRA_KEY_FROM_AMP, fromAmp)
                    putExtra(EXTRA_KEY_INCOGNITO, incognito)
                }
                ContextCompat.startForegroundService(it, webHeadLauncher)
            } else {
                Utils.openDrawOverlaySettings(it)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Singleton
class NativeFloatingBubble
@Inject
constructor(
        private val application: Application,
        private val websiteRepository: WebsiteRepository
) : FloatingBubble {

    private data class BubbleLoadData(
            val url: String,
            val fromMinimize: Boolean,
            val fromAmp: Boolean,
            val incognito: Boolean,
            val contextRef: WeakReference<Context?>
    )

    private val loadQueue = PublishSubject.create<BubbleLoadData>()

    private val nm by lazy {
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    init {
        loadQueue.onBackpressureBuffer()
                .observeOn(Schedulers.computation())
                .flatMap { bubbleData ->
                    websiteRepository
                            .getWebsite(bubbleData.url)
                            .subscribeOn(Schedulers.io())
                            .map { bubbleData to it }
                }.subscribe { (bubbleData, website) ->
                    (bubbleData.contextRef.get() ?: application).let { ctx ->

                        val browsingIntent = Intent(ctx, WebViewActivity::class.java).apply {
                            data = Uri.parse(bubbleData.url)
                        }
                        val bubbleIntent = PendingIntent.getActivity(
                                ctx,
                                bubbleData.url.hashCode(),
                                browsingIntent,
                                0
                        )
                        val channelId = "qbubbles"
                        var channel: NotificationChannel? = nm.getNotificationChannel(channelId)
                        if (channel == null) {
                            channel = NotificationChannel(channelId, "bubbles", NotificationManager.IMPORTANCE_HIGH)
                                    .apply { description = "Bubbles, bubble everywhere" }
                            nm.createNotificationChannel(channel)
                        }

                        val notification = Notification.Builder(ctx, channelId)
                                .setContentIntent(bubbleIntent)
                                .setGroup("bubble")
                                .setSmallIcon(Icon.createWithResource(ctx, R.drawable.ic_chromer_notification))
                                .setBubbleMetadata(Notification.BubbleMetadata.Builder().run {
                                    setDesiredHeight(1200)
                                    setIcon(Icon.createWithResource(ctx, R.mipmap.ic_launcher))
                                    setSuppressInitialNotification(false)
                                    setIntent(bubbleIntent)
                                    build()
                                })
                                .addPerson(Person.Builder().run {
                                    setBot(false)
                                    setIcon(Icon.createWithResource(ctx, R.mipmap.ic_launcher))
                                    setName(website.safeLabel())
                                    setImportant(false)
                                    build()
                                })
                                .build()

                        nm.notify(bubbleData.url.hashCode(), notification)
                    }
                }
    }


    override fun openBubble(
            url: String,
            fromMinimize: Boolean,
            fromAmp: Boolean,
            incognito: Boolean,
            context: Context?
    ) {
        loadQueue.onNext(BubbleLoadData(url, fromMinimize, fromAmp, incognito, WeakReference(context)))
    }
}