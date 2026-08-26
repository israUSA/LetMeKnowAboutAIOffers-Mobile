package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import com.letmeknow.aioffers.core.ui.LocalReduceMotion
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.Motion
import com.letmeknow.aioffers.ui.theme.glassSource
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Fondo aurora: fijo detrás de todo el contenido (el caller no debe meterlo dentro del
 * scroll). Tres manchas radiales flotando lentamente + una viñeta oscura que las apaga hacia
 * los bordes. Lleva [glassSource] para que las superficies de vidrio puedan difuminarlo.
 */
@Composable
fun AuroraBackground(hazeState: HazeState, modifier: Modifier = Modifier) {
    val indigoPhase by rememberFloatPhase(delayMillis = 0)
    val fuchsiaPhase by rememberFloatPhase(delayMillis = Motion.FloatBlobMillis / 3)
    val cyanPhase by rememberFloatPhase(delayMillis = Motion.FloatBlobMillis * 2 / 3)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .glassSource(hazeState),
    ) {
        drawBlob(
            phase = indigoPhase,
            color = AppColors.AuroraIndigo,
            center = Offset(size.width * 0.22f, size.height * 0.18f),
            radius = size.maxDimension * 0.55f,
            translateSign = Offset(1f, -1f),
        )
        drawBlob(
            phase = fuchsiaPhase,
            color = AppColors.AuroraFuchsia,
            center = Offset(size.width * 0.85f, size.height * 0.32f),
            radius = size.maxDimension * 0.5f,
            translateSign = Offset(-1f, 1f),
        )
        drawBlob(
            phase = cyanPhase,
            color = AppColors.AuroraCyan,
            center = Offset(size.width * 0.42f, size.height * 0.88f),
            radius = size.maxDimension * 0.6f,
            translateSign = Offset(1f, 1f),
        )
        drawVignette()
    }
}

/** Fase 0..1..0 de un blob, con delay propio para que no floten en sincronía. */
@Composable
private fun rememberFloatPhase(delayMillis: Int): State<Float> {
    val reduceMotion = LocalReduceMotion.current
    val duration = Motion.durationOrInstant(Motion.FloatBlobMillis, reduceMotion)
    if (duration == 0) return remember { mutableFloatStateOf(0f) }

    val transition = rememberInfiniteTransition(label = "floatBlob")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = Motion.EaseInOut),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis, StartOffsetType.FastForward),
        ),
        label = "floatBlobPhase",
    )
}

/** Desplazamiento 4-6% + scale hasta 1.08, según [phase] (0 = reposo, 1 = pico del flotado). */
private fun DrawScope.drawBlob(
    phase: Float,
    color: Color,
    center: Offset,
    radius: Float,
    translateSign: Offset,
) {
    val translateX = translateSign.x * size.width * 0.05f * phase
    val translateY = translateSign.y * size.height * 0.05f * phase
    val effectiveRadius = radius * (1f + 0.08f * phase)
    val effectiveCenter = center + Offset(translateX, translateY)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            center = effectiveCenter,
            radius = effectiveRadius,
        ),
        radius = effectiveRadius,
        center = effectiveCenter,
    )
}

/** Radial oscuro: transparente centro-arriba, [AppColors.AuroraVignetteEdge] hacia afuera. */
private fun DrawScope.drawVignette() {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, AppColors.AuroraVignetteEdge),
            center = Offset(size.width * 0.5f, 0f),
            radius = size.maxDimension * 0.9f,
        ),
        size = size,
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AuroraBackgroundPreview() {
    AppTheme {
        AuroraBackground(hazeState = rememberHazeState())
    }
}
