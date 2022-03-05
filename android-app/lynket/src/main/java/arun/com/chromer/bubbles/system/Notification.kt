package arun.com.chromer.bubbles.system

import android.app.Notification
import android.app.Person
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
inline fun notification(
  context: Context,
  channelId: String,
  crossinline builder: Notification.Builder.() -> Unit
): Notification = Notification.Builder(context, channelId).apply(builder).build()


@RequiresApi(Build.VERSION_CODES.Q)
inline fun Notification.Builder.bubbleMetadata(
  crossinline builder: Notification.BubbleMetadata.Builder.() -> Unit
) {
  setBubbleMetadata(Notification.BubbleMetadata.Builder().apply(builder).build())
}

@RequiresApi(Build.VERSION_CODES.P)
inline fun Notification.Builder.addPerson(
  crossinline builder: Person.Builder.() -> Unit
): Person = Person.Builder().apply(builder).build().also(::addPerson)

