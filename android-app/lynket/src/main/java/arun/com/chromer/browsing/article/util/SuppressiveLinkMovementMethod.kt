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

package arun.com.chromer.browsing.article.util

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast

import arun.com.chromer.R

/**
 * Created by arunk on 27-01-2018.
 */
object SuppressiveLinkMovementMethod : LinkMovementMethod() {
  override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
    return try {
      super.onTouchEvent(widget, buffer, event)
    } catch (ex: Exception) {
      Toast.makeText(widget.context, R.string.unsupported_link, Toast.LENGTH_LONG).show()
      true
    }

  }
}
