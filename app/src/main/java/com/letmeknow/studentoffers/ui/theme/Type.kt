package com.letmeknow.studentoffers.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.letmeknow.studentoffers.R

/**
 * Inter para todo el texto, Space Grotesk para display (wordmark de marca, título del hero,
 * nombre de la oferta en cada tarjeta).
 *
 * Ambas se empaquetan en `res/font` como fuentes variables en vez de descargarse de Google
 * Fonts: la app funciona offline y no depende de Play Services. `FontVariation` requiere
 * API 26, que es justo nuestro `minSdk`.
 */

private fun interFont(weight: Int) = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        // El eje `opsz` de Inter: 14 es el tamaño óptico pensado para texto de UI.
        FontVariation.Setting("opsz", 14f),
    ),
)

private fun spaceGroteskFont(weight: Int) = Font(
    resId = R.font.space_grotesk_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Fuente general. Pesos 400/500/600/700, igual que la web. */
val InterFamily = FontFamily(
    interFont(400),
    interFont(500),
    interFont(600),
    interFont(700),
)

/** Fuente de display. Pesos 500/600/700. */
val SpaceGroteskFamily = FontFamily(
    spaceGroteskFont(500),
    spaceGroteskFont(600),
    spaceGroteskFont(700),
)

val AppTypography = Typography().run {
    val body = TextStyle(fontFamily = InterFamily)
    copy(
        displayLarge = displayLarge.merge(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold),
        displayMedium = displayMedium.merge(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold),
        displaySmall = displaySmall.merge(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold),
        headlineLarge = headlineLarge.merge(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.merge(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.merge(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.merge(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.merge(body),
        titleSmall = titleSmall.merge(body),
        bodyLarge = bodyLarge.merge(body),
        bodyMedium = bodyMedium.merge(body),
        bodySmall = bodySmall.merge(body),
        labelLarge = labelLarge.merge(body),
        labelMedium = labelMedium.merge(body),
        labelSmall = labelSmall.merge(body),
    )
}

/**
 * Nombre de la empresa en la fila superior de cada tarjeta: mayúsculas chicas y espaciadas.
 * Se define acá para que la tarjeta y el bottom sheet de avisos no lo repliquen distinto.
 */
val CompanyLabelStyle = TextStyle(
    fontFamily = InterFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    letterSpacing = 0.8.sp,
)
