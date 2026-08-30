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
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letmeknow.studentoffers.R
import com.letmeknow.studentoffers.ui.theme.AppColors
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.BrandGradient
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.TextGradient
import com.letmeknow.studentoffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun Header(
    hazeState: HazeState,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
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
        Logo(modifier = Modifier.weight(1f, fill = false))
        AlertsButton(
            enabled = notificationsEnabled,
            onClick = { onNotificationsToggle(!notificationsEnabled) },
        )
    }
}

@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(Dimens.Shape2xl)
                .background(BrandGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 1.dp, end = 1.dp)
                    .size(10.dp),
            )
        }
        val baseWordmarkFontSize = MaterialTheme.typography.titleLarge.fontSize
        var wordmarkFontSize by remember { mutableStateOf(baseWordmarkFontSize) }
        Text(
            text = wordmark(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = wordmarkFontSize),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            onTextLayout = { result ->
                if (result.didOverflowWidth && wordmarkFontSize > MinWordmarkFontSize) {
                    wordmarkFontSize = (wordmarkFontSize.value * 0.92f).sp
                }
            },
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

private val MinWordmarkFontSize = 11.sp

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
private fun AlertsButton(enabled: Boolean, onClick: () -> Unit) {
    val stateLabel = stringResource(
        if (enabled) R.string.alerts_button_state_on else R.string.alerts_button_state_off,
    )

    Box(modifier = Modifier.size(Dimens.MinTouchTarget), contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .semantics { stateDescription = stateLabel },
        ) {
            Icon(
                imageVector = if (enabled) {
                    Icons.Filled.Notifications
                } else {
                    Icons.Filled.NotificationsOff
                },
                contentDescription = stringResource(R.string.alerts_button_description),
                tint = if (enabled) AppColors.Fuchsia500 else AppColors.OnBackgroundMuted,
            )
        }
        if (enabled) {
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
}

@Preview(name = "Avisos activados", showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun HeaderEnabledPreview() {
    AppTheme {
        Header(
            hazeState = rememberHazeState(),
            notificationsEnabled = true,
            onNotificationsToggle = {},
        )
    }
}

@Preview(name = "Avisos desactivados", showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun HeaderDisabledPreview() {
    AppTheme {
        Header(
            hazeState = rememberHazeState(),
            notificationsEnabled = false,
            onNotificationsToggle = {},
        )
    }
}
