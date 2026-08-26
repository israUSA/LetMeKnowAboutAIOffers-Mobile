package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.domain.model.ExpirationState
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.colors

/**
 * Badge de expiración: pastilla con texto, fondo y borde del `StateColorSet` del estado.
 *
 * @param label texto ya formateado — llega en `PromoUiModel.expirationLabel`. Este componente
 *   no calcula fechas ni pluraliza: `ExpirationRules` ya lo hizo.
 *
 * Cuando el estado es [ExpirationState.URGENT] este badge **no se muestra**: lo reemplaza
 * [CountdownTimer]. La decisión vive en `PromoCard`, no acá.
 */
@Composable
fun ExpirationBadge(
    label: String,
    state: ExpirationState,
    modifier: Modifier = Modifier,
) {
    StatePill(state = state, modifier = modifier) {
        Text(
            text = label,
            color = state.colors.text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Chrome compartido entre el badge y el countdown urgente.
 *
 * Existe porque DESIGN_SYSTEM.md dice que en `urgent` el countdown **reemplaza** al badge: los
 * dos ocupan el mismo lugar y deben verse como la misma pieza. Es un slot para que el
 * countdown pueda meter su propio contenido sin duplicar fondo, borde y padding.
 */
@Composable
internal fun StatePill(
    state: ExpirationState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = state.colors
    Row(
        modifier = modifier
            .clip(Dimens.ShapePill)
            .background(colors.badgeBackground)
            .border(width = 1.dp, color = colors.badgeBorder, shape = Dimens.ShapePill)
            .padding(horizontal = Dimens.CardSpacing, vertical = Dimens.CardSpacing / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing / 2),
        content = content,
    )
}

@Preview(name = "ExpirationBadge", widthDp = 360, showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun ExpirationBadgePreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(AppColors.Background)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            horizontalAlignment = Alignment.Start,
        ) {
            ExpirationBadge(label = "Siempre disponible", state = ExpirationState.PERMANENT)
            ExpirationBadge(label = "Expira el 15 mar 2027", state = ExpirationState.COMFORTABLE)
            ExpirationBadge(label = "Expira en 12 días", state = ExpirationState.WARNING)
            // Solo para comparar el chrome; en `urgent` real se muestra el countdown.
            ExpirationBadge(label = "Expira mañana", state = ExpirationState.URGENT)
        }
    }
}
