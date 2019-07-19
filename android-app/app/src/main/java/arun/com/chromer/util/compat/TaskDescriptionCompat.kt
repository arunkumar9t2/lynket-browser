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

package arun.com.chromer.util.compat

import android.app.ActivityManager
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi

/**
 * Created by arunk on 09-02-2018.
 */
data class TaskDescriptionCompat(
        var label: String?,
        var icon: Bitmap?,
        @ColorInt var color: Int
) {
    /**
     * Convert to [ActivityManager.TaskDescription]
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun toActivityTaskDescription(): ActivityManager.TaskDescription = ActivityManager.TaskDescription(label, icon, color)
}