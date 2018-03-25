/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import arun.com.chromer.util.Utils

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

/**
 * Does a circular hide animation on this view with center point location at the center of the view.
 * Does not do anything if the view is not laid and if the device is not above lollipop.
 */
@SuppressLint("NewApi")
fun View.circularHideWithSelfCenter(done: (() -> Unit)? = null) {
    if (Utils.ANDROID_LOLLIPOP && isLaidOut && visibility == View.VISIBLE) {
        // get the center for the clipping circle
        val cx = width / 2
        val cy = height / 2
        // get the initial radius for the clipping circle
        val initialRadius = Math.hypot(cx.toDouble(), cy.toDouble())

        // create the animation (the final radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, initialRadius.toFloat(), 0f)
        anim.apply {
            // make the view invisible when the animation is done
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    this@circularHideWithSelfCenter.gone()
                    done?.invoke()
                }
            })
            // start the animation
            start()
        }
    } else hide()
}

/**
 * Opposite of [circularHideWithSelfCenter]
 */
@SuppressLint("NewApi")
fun View.circularRevealWithSelfCenter(done: (() -> Unit)? = null) {
    if (Utils.ANDROID_LOLLIPOP && isLaidOut && visibility != View.VISIBLE) {
        // get the center for the clipping circle
        val cx = width / 2
        val cy = height / 2
        // get the initial radius for the clipping circle
        val initialRadius = Math.hypot(cx.toDouble(), cy.toDouble())

        // create the animation (the start radius is zero)
        ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, initialRadius.toFloat()).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    done?.invoke()
                }
            })
            // Make the view visible
            show()
            // Start the animation
            start()
        }
    } else show()
}


fun View.children(): ArrayList<View> {
    val children = ArrayList<View>()
    if (this is ViewGroup) {
        (0 until childCount).mapTo(children) { getChildAt(it) }
    }
    return children
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}
