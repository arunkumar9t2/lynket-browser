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

package arun.com.chromer.extenstions

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

/**
 * Created by arunk on 03-02-2018.
 */

/**
 * Set progress with animation if [Build.VERSION_CODES.N].
 *
 * Hides the progress bar when @param progress reaches 100.
 */
fun MaterialProgressBar.setAutoHideProgress(newProgress: Int, tint: ColorStateList) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        setProgress(newProgress, true)
    } else {
        progress = newProgress
    }
    val transparentTint = ColorStateList.valueOf(Color.TRANSPARENT)
    if (newProgress == 100) {
        postDelayed({ progressTintList = transparentTint }, 200)
    } else if (progressTintList != tint) {
        progressTintList = tint
    }
}
