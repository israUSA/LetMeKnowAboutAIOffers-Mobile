package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.SpaceGroteskFamily
import com.letmeknow.aioffers.ui.theme.companyColor

/**
 * Avatar circular de la empresa: la inicial en blanco sobre el color de marca de
 * `COMPANY_COLORS` (ver `companyColor`).
 *
 * Es **decorativo**: la fila superior de la tarjeta ya muestra el nombre de la empresa como
 * texto, así que repetir la inicial en el árbol de accesibilidad solo agregaría ruido. Por eso
 * el nodo se limpia con `clearAndSetSemantics {}` en vez de llevar `contentDescription`.
 */
@Composable
fun CompanyAvatar(
    company: String,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.CompanyAvatarSize,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = companyColor(company), shape = CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = company.avatarInitial(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

/**
 * Primera letra visible del nombre, en mayúscula. Un nombre vacío o solo con espacios cae en
 * `?` en vez de dejar el círculo mudo.
 */
private fun String.avatarInitial(): String =
    trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

@Preview(name = "CompanyAvatar", widthDp = 360, showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun CompanyAvatarPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(AppColors.Background)
                .padding(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf("GitHub Education", "Google", "JetBrains", "Figma", "Notion", "AWS", "Otra", " ")
                .forEach { CompanyAvatar(company = it) }
        }
    }
}

@Preview(name = "CompanyAvatar grande", showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun CompanyAvatarLargePreview() {
    AppTheme {
        Box(Modifier.padding(Dimens.ScreenPadding)) {
            CompanyAvatar(company = "Microsoft Azure", size = 72.dp)
        }
    }
}
