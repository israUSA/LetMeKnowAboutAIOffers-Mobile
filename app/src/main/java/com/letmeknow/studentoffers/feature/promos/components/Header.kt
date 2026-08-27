package com.letmeknow.studentoffers.feature.promos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.studentoffers.R
import com.letmeknow.studentoffers.ui.theme.AppColors
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.BrandGradient
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.TextGradient
import com.letmeknow.studentoffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Header fijo arriba de la pantalla: logo (ícono con [BrandGradient] + wordmark con
 * [TextGradient]) a la izquierda, botón de campana con punto indicador a la derecha.
 *
 * [onAlertsClick] abre el destino de avisos (`feature/alerts`). El punto indicador es fijo:
 * hoy no hay contador de avisos sin leer que lo condicione.
 */
@Composable
fun Header(
    hazeState: HazeState,
    onAlertsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(state = hazeState, shape = Dimens.Shape2xl)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Logo()
        AlertsButton(onClick = onAlertsClick)
    }
}

@Composable
private fun Logo() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(Dimens.Shape2xl)
                .background(BrandGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = wordmark(),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun wordmark() = buildAnnotatedString {
    withStyle(SpanStyle(brush = TextGradient)) {
        append(stringResource(R.string.wordmark_brand))
    }
    withStyle(SpanStyle(color = AppColors.OnBackground)) {
        append(stringResource(R.string.wordmark_rest))
    }
}

@Composable
private fun AlertsButton(onClick: () -> Unit) {
    Box(modifier = Modifier.size(Dimens.MinTouchTarget), contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick, modifier = Modifier.size(Dimens.MinTouchTarget)) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = stringResource(R.string.alerts_button_description),
                tint = LocalContentColor.current,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(AppColors.Fuchsia500),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun HeaderPreview() {
    AppTheme {
        Header(hazeState = rememberHazeState(), onAlertsClick = {})
    }
}
