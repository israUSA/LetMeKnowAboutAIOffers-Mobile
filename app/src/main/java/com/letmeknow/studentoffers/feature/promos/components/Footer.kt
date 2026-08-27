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
import com.letmeknow.studentoffers.ui.theme.TextGradient

/** Wordmark partido en [TextGradient] + color normal, y disclaimer sobre verificar en el sitio oficial. */
@Composable
fun Footer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = wordmark(),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.footer_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.OnBackgroundMuted,
            textAlign = TextAlign.Center,
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

@Preview(showBackground = true, backgroundColor = 0xFF060610, widthDp = 360)
@Composable
private fun FooterPreview() {
    AppTheme {
        Footer()
    }
}
