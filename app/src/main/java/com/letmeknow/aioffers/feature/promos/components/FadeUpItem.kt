package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.core.ui.LocalReduceMotion
import com.letmeknow.aioffers.ui.theme.Motion
import kotlinx.coroutines.delay

/**
 * `fade-up`: opacity 0→1 + translateY 18dp→0 + scale .98→1, tras un [delayMillis] propio.
 * Usado por el hero (delays escalonados) y por la cascada de la grilla (una instancia por
 * tarjeta, `delay = Motion.staggerDelay(index, reduceMotion)`).
 *
 * Con `LocalReduceMotion` activo salta directo al estado final: ni delay ni animación.
 */
@Composable
fun FadeUpItem(
    delayMillis: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduceMotion = LocalReduceMotion.current
    val duration = Motion.durationOrInstant(Motion.FadeUpMillis, reduceMotion)
    var visible by remember { mutableStateOf(reduceMotion) }

    LaunchedEffect(delayMillis, reduceMotion) {
        if (!reduceMotion) {
            delay(delayMillis.toLong())
        }
        visible = true
    }

    // `rememberTransition` recibe un TransitionState ya construido; para animar hacia un valor
    // objetivo la API correcta es `updateTransition`.
    val transition = updateTransition(targetState = visible, label = "fadeUp")
    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = duration, easing = Motion.EaseOutBack) },
        label = "fadeUpAlpha",
    ) { isVisible -> if (isVisible) 1f else 0f }
    val scale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = duration, easing = Motion.EaseOutBack) },
        label = "fadeUpScale",
    ) { isVisible -> if (isVisible) 1f else Motion.FadeUpInitialScale }
    val translateY by transition.animateDp(
        transitionSpec = { tween(durationMillis = duration, easing = Motion.EaseOutBack) },
        label = "fadeUpTranslateY",
    ) { isVisible -> if (isVisible) 0.dp else Motion.FadeUpTranslation }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translateY.toPx()
            this.scaleX = scale
            this.scaleY = scale
        },
    ) {
        content()
    }
}
