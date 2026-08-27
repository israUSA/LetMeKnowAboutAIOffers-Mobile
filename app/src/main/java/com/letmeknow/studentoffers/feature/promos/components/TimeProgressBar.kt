package com.letmeknow.studentoffers.feature.promos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.ui.theme.AppColors
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.colors

/**
 * Barra muy fina, al borde superior de la tarjeta, que representa cuánto tiempo le queda a la
 * oferta. Solo se dibuja cuando la oferta tiene vencimiento.
 *
 * @param percent porcentaje de tiempo **restante**, 0..100. Viene ya calculado en
 *   `PromoUiModel.timeRemainingPercent`; acá no se recalcula nada.
 * @param state define el gradiente horizontal (`StateColorSet.progressBrush`).
 *
 * No aporta semantics: la misma información se anuncia como texto en el badge de expiración,
 * y duplicarla haría que TalkBack lea dos veces lo mismo.
 */
@Composable
fun TimeProgressBar(
    percent: Float,
    state: ExpirationState,
    modifier: Modifier = Modifier,
) {
    val fraction = (percent / 100f).coerceIn(0f, 1f)
    // `progressBrush` es un getter: sin `remember` se construiría un Brush nuevo por recomposición.
    val brush = remember(state) { state.colors.progressBrush }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.ProgressBarHeight)
            .background(AppColors.GlassSurfaceLow)
            .clearAndSetSemantics { },
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .background(brush),
        )
    }
}

@Preview(name = "TimeProgressBar", widthDp = 360, showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun TimeProgressBarPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(AppColors.Background)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
        ) {
            TimeProgressBar(percent = 92f, state = ExpirationState.PERMANENT)
            TimeProgressBar(percent = 64f, state = ExpirationState.COMFORTABLE)
            TimeProgressBar(percent = 28f, state = ExpirationState.WARNING)
            TimeProgressBar(percent = 6f, state = ExpirationState.URGENT)
            // Fuera de rango por ambos lados: se recorta a 0..1, no revienta.
            TimeProgressBar(percent = -30f, state = ExpirationState.URGENT)
            TimeProgressBar(percent = 180f, state = ExpirationState.COMFORTABLE)
        }
    }
}
