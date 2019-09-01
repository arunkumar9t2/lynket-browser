package arun.com.chromer.util.animations

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.view.View
import android.view.View.*
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce


private fun getKey(property: DynamicAnimation.ViewProperty) = when (property) {
    SpringAnimation.TRANSLATION_X -> TRANSLATION_X.name
    SpringAnimation.TRANSLATION_Y -> TRANSLATION_Y.name
    SpringAnimation.TRANSLATION_Z -> if (SDK_INT >= LOLLIPOP) {
        TRANSLATION_Z.name
    } else {
        "TRANSLATION_Z"
    }
    SpringAnimation.SCALE_X -> SCALE_X.name
    SpringAnimation.SCALE_Y -> SCALE_Y.name
    SpringAnimation.ROTATION -> ROTATION.name
    SpringAnimation.ROTATION_X -> ROTATION_X.name
    SpringAnimation.ROTATION_Y -> ROTATION_Y.name
    SpringAnimation.X -> X.name
    SpringAnimation.Y -> Y.name
    SpringAnimation.Z -> if (SDK_INT >= LOLLIPOP) {
        Z.name
    } else {
        "Z"
    }
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