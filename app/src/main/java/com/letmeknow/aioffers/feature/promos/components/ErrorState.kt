package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.R
import com.letmeknow.aioffers.feature.promos.ErrorKind
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.BrandGradient
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Mensaje legible y accionable por [ErrorKind]. La app nunca queda en blanco por esto:
 * `MissingConfig` dice explícitamente qué claves faltan y que se configuran en
 * `local.properties`; `Network`, `Http` y `MalformedPayload` llevan mensajes propios.
 */
@Composable
fun ErrorState(
    kind: ErrorKind,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val (icon, title, body) = errorContent(kind)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(state = hazeState, shape = Dimens.Shape3xl)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Rose300,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnBackgroundMuted,
            textAlign = TextAlign.Center,
        )

        // MissingConfig no ofrece reintento: no se arregla tocando un botón, sino editando
        // `local.properties` y recompilando. Un botón ahí solo repetiría el mismo error.
        if (onRetry != null && kind !is ErrorKind.MissingConfig) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier
                    .heightIn(min = Dimens.MinTouchTarget)
                    .background(BrandGradient, Dimens.ShapePill),
            ) {
                Text(
                    text = stringResource(R.string.error_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.OnBackground,
                )
            }
        }
    }
}

private data class ErrorContent(val icon: ImageVector, val title: String, val body: String)

@Composable
private fun errorContent(kind: ErrorKind): ErrorContent = when (kind) {
    is ErrorKind.MissingConfig -> ErrorContent(
        icon = Icons.Filled.SettingsSuggest,
        title = stringResource(R.string.error_missing_config_title),
        body = stringResource(
            R.string.error_missing_config_body,
            kind.missingKeys.joinToString(", "),
        ),
    )

    ErrorKind.Network -> ErrorContent(
        icon = Icons.Filled.CloudOff,
        title = stringResource(R.string.error_network_title),
        body = stringResource(R.string.error_network_body),
    )

    is ErrorKind.Http -> ErrorContent(
        icon = Icons.Filled.WarningAmber,
        title = stringResource(R.string.error_http_title),
        body = stringResource(R.string.error_http_body, kind.code),
    )

    ErrorKind.MalformedPayload -> ErrorContent(
        icon = Icons.Filled.ErrorOutline,
        title = stringResource(R.string.error_malformed_title),
        body = stringResource(R.string.error_malformed_body),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun ErrorStateMissingConfigPreview() {
    AppTheme {
        ErrorState(
            kind = ErrorKind.MissingConfig(missingKeys = listOf("SUPABASE_URL", "SUPABASE_ANON_KEY")),
            hazeState = rememberHazeState(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun ErrorStateNetworkPreview() {
    AppTheme {
        ErrorState(kind = ErrorKind.Network, hazeState = rememberHazeState())
    }
}
