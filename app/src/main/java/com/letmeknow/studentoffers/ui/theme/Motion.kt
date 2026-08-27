package com.letmeknow.studentoffers.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.unit.dp

/**
 * Las cuatro animaciones que especifica DESIGN_SYSTEM.md, con sus duraciones exactas.
 *
 * REGLA: toda animación debe consultar `LocalReduceMotion` y saltarse si está activo.
 * `durationOrInstant` está justo para eso — usalo en vez de pasar estas constantes crudas.
 */
object Motion {

    /** `fade-up`: opacity 0→1, translateY 18dp→0, scale .98→1. */
    const val FadeUpMillis = 550
    val FadeUpTranslation = 18.dp
    const val FadeUpInitialScale = 0.98f

    /** Cascada de la grilla: delay = índice × 55ms, con tope de 500ms. */
    const val StaggerStepMillis = 55
    const val StaggerMaxMillis = 500

    /** Delays escalonados de los elementos del hero. */
    val HeroStaggerMillis = listOf(60, 120, 180)

    /** `float-blob`: translate + scale sutil de los blobs del aurora, loop infinito. */
    const val FloatBlobMillis = 18_000

    /** `pulse-slow`: opacity 1→0.6→1 del countdown urgente ("respiración"). */
    const val PulseSlowMillis = 3_000
    const val PulseMinAlpha = 0.6f

    /** `shimmer`: barrido de brillo de los skeletons de carga. */
    const val ShimmerMillis = 1_600

    /** Expandir/colapsar la descripción de una tarjeta. Nunca un salto abrupto. */
    const val ExpandMillis = 300

    /** Rotación del chevron al expandir. */
    const val ChevronMillis = 300

    /**
     * Easing tipo "ease-out-back": sobrepasa levemente el destino y vuelve. Es lo que le da
     * al `fade-up` su carácter; con un ease-out común se siente plano.
     */
    val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    /** Easing suave y simétrico para el flotado de los blobs y el pulse. */
    val EaseInOut: Easing = FastOutSlowInEasing

    val Linear: Easing = LinearEasing

    /**
     * Devuelve `0` cuando el usuario pidió reducir movimiento, y la duración original si no.
     * Una duración de cero hace que Compose salte directo al valor final, que es exactamente
     * el comportamiento que se busca: sin animación, pero con el estado final correcto.
     */
    fun durationOrInstant(millis: Int, reduceMotion: Boolean): Int = if (reduceMotion) 0 else millis

    /** Delay de la cascada para la tarjeta en la posición [index]. */
    fun staggerDelay(index: Int, reduceMotion: Boolean): Int =
        if (reduceMotion) 0 else (index * StaggerStepMillis).coerceAtMost(StaggerMaxMillis)
}
