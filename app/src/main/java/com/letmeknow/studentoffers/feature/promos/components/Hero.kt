package com.letmeknow.studentoffers.feature.promos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.studentoffers.R
import com.letmeknow.studentoffers.ui.theme.AppColors
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.Motion
import com.letmeknow.studentoffers.ui.theme.TextGradient
import com.letmeknow.studentoffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Badge de conteo, título con parte en [TextGradient], subtítulo y [SearchField]. Entrada
 * con `fade-up` escalonado según [Motion.HeroStaggerMillis].
 */
@Composable
fun Hero(
    verifiedCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FadeUpItem(delayMillis = Motion.HeroStaggerMillis[0]) {
            VerifiedBadge(count = verifiedCount, hazeState = hazeState)
        }
        FadeUpItem(delayMillis = Motion.HeroStaggerMillis[1]) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = heroTitle(),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.hero_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.OnBackgroundMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
        FadeUpItem(delayMillis = Motion.HeroStaggerMillis[2], modifier = Modifier.fillMaxWidth()) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                hazeState = hazeState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun heroTitle() = buildAnnotatedString {
    withStyle(SpanStyle(color = Color.White)){
        append(stringResource(R.string.hero_title_prefix))
    }
    withStyle(SpanStyle(brush = TextGradient)) {
        append(stringResource(R.string.hero_title_gradient))
    }
}

@Composable
private fun VerifiedBadge(count: Int, hazeState: HazeState) {
    Text(
        text = pluralStringResource(R.plurals.hero_badge_verified_count, count, count),
        style = MaterialTheme.typography.labelLarge,
        color = AppColors.OnBackground,
        modifier = Modifier
            .glassSurface(state = hazeState, shape = Dimens.ShapePill)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun HeroPreview() {
    AppTheme {
        Hero(
            verifiedCount = 42,
            query = "",
            onQueryChange = {},
            hazeState = rememberHazeState(),
        )
    }
}
