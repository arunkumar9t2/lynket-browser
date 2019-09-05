package arun.com.chromer.bubbles.system

import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import arun.com.chromer.R
import arun.com.chromer.browsing.webview.WebViewActivity
import arun.com.chromer.shared.Constants
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

private const val BUBBLE_NOTIFICATION_CHANNEL_ID = "BUBBLE_NOTIFICATION_CHANNEL_ID"
private const val BUBBLE_NOTIFICATION_GROUP = "bubbles"

@Singleton
@RequiresApi(Build.VERSION_CODES.Q)
class BubbleNotificationUtil
@Inject
constructor(
        private val application: Application
) {

    private val notificationManager by lazy {
        application.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationChannel() {
        var channel = notificationManager.getNotificationChannel(BUBBLE_NOTIFICATION_CHANNEL_ID)
        if (channel == null) {
            channel = NotificationChannel(
                    BUBBLE_NOTIFICATION_CHANNEL_ID,
                    "Bubbles notification channel",
                    IMPORTANCE_HIGH
            ).apply {
                description = "Channel for showing bubbles"
                setAllowBubbles(true)
                setSound(null, null)
                enableLights(false)
                enableVibration(false)
                setBypassDnd(true)
                importance = IMPORTANCE_HIGH
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBubbles(bubbleData: BubbleLoadData): Single<BubbleLoadData> = Single.fromCallable {
        createNotificationChannel()
        val context = bubbleData.contextRef.get() ?: application
        val website = bubbleData.website

        val bubbleIntent = PendingIntent.getActivity(
                context,
                website.url.hashCode(),
                Intent(context, WebViewActivity::class.java).apply {
                    data = Uri.parse(website.url)
                },
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val bubbleIcon: Icon = bubbleData.icon?.let(Icon::createWithBitmap)
                ?: Icon.createWithResource(context, R.mipmap.ic_launcher)

        val bubbleNotification = Notification.Builder(context, BUBBLE_NOTIFICATION_CHANNEL_ID).run {
            setContentTitle(website.safeLabel())
            setContentText(website.preferredUrl())
            setGroup(BUBBLE_NOTIFICATION_GROUP)
            setAllowSystemGeneratedContextualActions(false)
            bubbleData.color.takeIf { it != Constants.NO_COLOR }?.let(::setColor)
            setColorized(true)
            setLocalOnly(true)
            setOngoing(false) // TODO Register a broadcast receiver and call notificationManager.cancel
            setSmallIcon(Icon.createWithResource(context, R.drawable.ic_chromer_notification))
            setLargeIcon(bubbleIcon)
            setBubbleMetadata(Notification.BubbleMetadata.Builder().run {
                setIcon(bubbleIcon)
                setIntent(bubbleIntent)
                setAutoExpandBubble(false)
                setSuppressNotification(true)
                build()
            })
            addPerson(Person.Builder().run {
                setBot(false)
                setIcon(bubbleIcon)
                setName(website.safeLabel())
                setImportant(true)
                build()
            })
            build()
        }
        notificationManager.notify(website.url.hashCode(), bubbleNotification)
        bubbleData
    }
}