package com.letmeknow.studentoffers.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.unit.dp

object Motion {

    const val FadeUpMillis = 550
    val FadeUpTranslation = 18.dp
    const val FadeUpInitialScale = 0.98f

    const val StaggerStepMillis = 55
    const val StaggerMaxMillis = 500

    const val StaggerItemLimit = StaggerMaxMillis / StaggerStepMillis

    val HeroStaggerMillis = listOf(60, 120, 180)

    const val FloatBlobMillis = 18_000

    const val PulseSlowMillis = 3_000
    const val PulseMinAlpha = 0.6f

    const val ShimmerMillis = 1_600

    const val ExpandMillis = 300

    const val ChevronMillis = 300

    val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    val EaseInOut: Easing = FastOutSlowInEasing

    val Linear: Easing = LinearEasing

    fun durationOrInstant(millis: Int, reduceMotion: Boolean): Int = if (reduceMotion) 0 else millis

    fun staggerDelay(index: Int, reduceMotion: Boolean): Int =
        if (reduceMotion) 0 else (index * StaggerStepMillis).coerceAtMost(StaggerMaxMillis)
}
