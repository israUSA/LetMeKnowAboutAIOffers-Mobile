package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.core.ui.LocalReduceMotion
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.Motion
import com.letmeknow.aioffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Placeholder con la misma forma que la tarjeta real (TODO(feat/promo-card)): barra de
 * progreso arriba, avatar + nombre/título, fila inferior de badge + botón. `shimmer`:
 * barrido de brillo horizontal, 1.6s, loop infinito.
 */
@Composable
fun SkeletonCard(hazeState: HazeState, modifier: Modifier = Modifier) {
    val progress by rememberShimmerProgress()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(state = hazeState, shape = Dimens.Shape2xl)
            .shimmerSweep(progress)
            .padding(Dimens.CardPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlaceholderBlock(modifier = Modifier.fillMaxWidth().height(Dimens.ProgressBarHeight))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PlaceholderBlock(
                modifier = Modifier.size(Dimens.CompanyAvatarSize),
                shape = CircleShape,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceholderBlock(modifier = Modifier.width(80.dp).height(10.dp))
                PlaceholderBlock(modifier = Modifier.fillMaxWidth().height(16.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceholderBlock(modifier = Modifier.width(96.dp).height(24.dp), shape = Dimens.ShapePill)
            PlaceholderBlock(modifier = Modifier.width(88.dp).height(36.dp), shape = Dimens.ShapePill)
        }
    }
}

@Composable
private fun PlaceholderBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(AppColors.GlassSurfaceHigh),
    )
}

@Composable
private fun rememberShimmerProgress(): State<Float> {
    val reduceMotion = LocalReduceMotion.current
    val duration = Motion.durationOrInstant(Motion.ShimmerMillis, reduceMotion)
    if (duration == 0) return remember { mutableFloatStateOf(0f) }

    val transition = rememberInfiniteTransition(label = "shimmer")
    return transition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = Motion.EaseInOut),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )
}

/** Superpone una franja de brillo que barre horizontalmente según [progress] (0 = izquierda, 1 = derecha). */
private fun Modifier.shimmerSweep(progress: Float): Modifier = drawWithContent {
    drawContent()
    val bandWidth = size.width * 0.35f
    val x = progress * size.width
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.14f), Color.Transparent),
            start = Offset(x - bandWidth, 0f),
            end = Offset(x + bandWidth, size.height),
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun SkeletonCardPreview() {
    AppTheme {
        SkeletonCard(hazeState = rememberHazeState())
    }
}
