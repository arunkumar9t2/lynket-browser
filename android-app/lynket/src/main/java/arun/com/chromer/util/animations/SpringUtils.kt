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

package arun.com.chromer.util.animations

import android.view.View
import android.view.View.*
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce


private fun getKey(property: DynamicAnimation.ViewProperty) = when (property) {
  SpringAnimation.TRANSLATION_X -> TRANSLATION_X.name
  SpringAnimation.TRANSLATION_Y -> TRANSLATION_Y.name
  SpringAnimation.TRANSLATION_Z -> TRANSLATION_Z.name
  SpringAnimation.SCALE_X -> SCALE_X.name
  SpringAnimation.SCALE_Y -> SCALE_Y.name
  SpringAnimation.ROTATION -> ROTATION.name
  SpringAnimation.ROTATION_X -> ROTATION_X.name
  SpringAnimation.ROTATION_Y -> ROTATION_Y.name
  SpringAnimation.X -> X.name
  SpringAnimation.Y -> Y.name
  SpringAnimation.Z -> Z.name
  SpringAnimation.ALPHA -> ALPHA.name
  SpringAnimation.SCROLL_X -> "SCROLL_X"
  SpringAnimation.SCROLL_Y -> "SCROLL_Y"
  else -> throw IllegalAccessException("Unknown ViewProperty: $property")
}.hashCode()


fun View.spring(
  property: DynamicAnimation.ViewProperty,
  stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
  damping: Float = SpringForce.DAMPING_RATIO_NO_BOUNCY,
  startVelocity: Float? = null
): SpringAnimation {
  val key = getKey(property)
  var springAnim = getTag(key) as? SpringAnimation?
  if (springAnim == null) {
    springAnim = SpringAnimation(this, property).apply {
      spring = SpringForce().apply {
        this.dampingRatio = damping
        this.stiffness = stiffness
        startVelocity?.let(::setStartVelocity)
      }
    }
    setTag(key, springAnim)
  }
  return springAnim
}
