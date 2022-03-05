/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

