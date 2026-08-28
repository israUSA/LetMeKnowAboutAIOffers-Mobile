package com.letmeknow.studentoffers.feature.promos.components

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.letmeknow.studentoffers.ui.brand.CompanyIcon
import com.letmeknow.studentoffers.ui.brand.LogoSource
import com.letmeknow.studentoffers.ui.brand.rememberCompanyIconVector
import com.letmeknow.studentoffers.ui.brand.resolveLogoSource
import com.letmeknow.studentoffers.ui.theme.AppColors
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.SpaceGroteskFamily
import com.letmeknow.studentoffers.ui.theme.companyColor

/** Fracción del círculo que ocupa el logo (ícono vectorial o favicon), dejando aire alrededor. */
private const val LogoContentScale = 0.6f

/** Fondo claro sobre el que se leen los logos a color: reemplaza el color sólido de marca. */
private val LogoBackdrop = Color(0xFFF1F5F9)

/**
 * Avatar circular de la empresa: cascada de 3 niveles (`improve-features.md`).
 *
 * 1. Ícono de [Simple Icons](https://simpleicons.org) a color, si la empresa está mapeada en
 *    `ui/brand/CompanyIcons.kt`.
 * 2. Si no, el favicon de su dominio real (`ui/brand/CompanyDomains.kt`) pedido en vivo con
 *    Coil. Si la carga falla —sin red, dominio caído— degrada al nivel 3 en vez de dejar un
 *    hueco o un ícono roto.
 * 3. Si la empresa no está en ninguno de los dos mapas: la inicial en blanco sobre el color de
 *    marca de `COMPANY_COLORS` (ver `companyColor`) — el comportamiento original, ahora como
 *    último recurso. Cualquier empresa nueva sin mapear cae acá sin romperse.
 *
 * Es **decorativo** en los tres niveles: la fila superior de la tarjeta ya muestra el nombre de
 * la empresa como texto, así que repetirlo en el árbol de accesibilidad solo agregaría ruido.
 * Por eso el nodo se limpia con `clearAndSetSemantics {}` en vez de llevar `contentDescription`.
 */
@Composable
fun CompanyAvatar(
    company: String,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.CompanyAvatarSize,
) {
    when (val source = remember(company) { resolveLogoSource(company) }) {
        is LogoSource.SimpleIcon -> SimpleIconAvatar(icon = source.icon, modifier = modifier, size = size)
        is LogoSource.Favicon ->
            FaviconAvatar(company = company, url = source.url, modifier = modifier, size = size)

        LogoSource.Fallback -> InitialAvatar(company = company, modifier = modifier, size = size)
    }
}

/** Nivel 1: ícono vectorial de Simple Icons, a color, sobre fondo claro. */
@Composable
private fun SimpleIconAvatar(
    icon: CompanyIcon,
    modifier: Modifier,
    size: Dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = LogoBackdrop, shape = CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = rememberCompanyIconVector(icon),
            contentDescription = null,
            modifier = Modifier.size(size * LogoContentScale),
        )
    }
}

/**
 * Nivel 2: favicon del dominio real, pedido en vivo con Coil sobre fondo claro. El estado de
 * error de `AsyncImage` es lo que dispara la degradación al nivel 3: sin red o con el dominio
 * caído, `isError` pasa a `true` y esta función delega directo en [InitialAvatar].
 */
@Composable
private fun FaviconAvatar(
    company: String,
    url: String,
    modifier: Modifier,
    size: Dp,
) {
    var isError by remember(url) { mutableStateOf(false) }

    if (isError) {
        InitialAvatar(company = company, modifier = modifier, size = size)
        return
    }

    Box(
        modifier = modifier
            .size(size)
            .background(color = LogoBackdrop, shape = CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            onError = { isError = true },
            modifier = Modifier.size(size * LogoContentScale),
        )
    }
}

/**
 * Nivel 3: la inicial en blanco sobre el color de marca de `COMPANY_COLORS` (ver
 * `companyColor`). Comportamiento original, sin cambios.
 */
@Composable
private fun InitialAvatar(
    company: String,
    modifier: Modifier,
    size: Dp,
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
            // "GitHub" y "Figma" -> nivel 1 (Simple Icons). "Microsoft" y "AWS" -> nivel 2
            // (favicon en vivo). "GitHub Education" no matchea el slug exacto de Simple Icons,
            // así que sigue cayendo en el nivel 3 con su color propio de `companyColor`. "Otra"
            // y " " son empresas sin mapear: nivel 3 genérico.
            listOf("GitHub", "GitHub Education", "Figma", "Notion", "Microsoft", "AWS", "Otra", " ")
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
