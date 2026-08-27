package com.letmeknow.studentoffers.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.letmeknow.studentoffers.domain.model.ExpirationState

/**
 * Los cinco colores coherentes que DESIGN_SYSTEM.md asigna a cada estado de expiración:
 * texto del badge, fondo del badge, borde del badge, gradiente de la barra de progreso,
 * y el glow de la tarjeta.
 */
@Immutable
data class StateColorSet(
    val text: Color,
    val badgeBackground: Color,
    val badgeBorder: Color,
    val progressStart: Color,
    val progressEnd: Color,
) {
    /** Gradiente horizontal de la barra de progreso de tiempo. */
    val progressBrush: Brush get() = Brush.horizontalGradient(listOf(progressStart, progressEnd))

    /** Color del glow al destacar la tarjeta. Mismo tono que el texto, muy tenue. */
    val glow: Color get() = text.copy(alpha = 0.25f)
}

/** Equivalente de `STATE_COLORS` de la web. */
val ExpirationState.colors: StateColorSet
    get() = when (this) {
        ExpirationState.PERMANENT -> StateColorSet(
            text = AppColors.Emerald300,
            badgeBackground = AppColors.Emerald300.copy(alpha = 0.10f),
            badgeBorder = AppColors.Emerald300.copy(alpha = 0.25f),
            progressStart = AppColors.Emerald400,
            progressEnd = AppColors.Teal500,
        )

        ExpirationState.COMFORTABLE -> StateColorSet(
            text = AppColors.Sky300,
            badgeBackground = AppColors.Sky300.copy(alpha = 0.10f),
            badgeBorder = AppColors.Sky300.copy(alpha = 0.25f),
            progressStart = AppColors.Sky400,
            progressEnd = AppColors.Cyan500,
        )

        ExpirationState.WARNING -> StateColorSet(
            text = AppColors.Amber300,
            badgeBackground = AppColors.Amber300.copy(alpha = 0.10f),
            badgeBorder = AppColors.Amber300.copy(alpha = 0.25f),
            progressStart = AppColors.Amber400,
            progressEnd = AppColors.Orange500,
        )

        ExpirationState.URGENT -> StateColorSet(
            text = AppColors.Rose300,
            badgeBackground = AppColors.Rose300.copy(alpha = 0.12f),
            badgeBorder = AppColors.Rose300.copy(alpha = 0.28f),
            progressStart = AppColors.Rose400,
            progressEnd = AppColors.Red500,
        )
    }

/**
 * Equivalente de `COMPANY_COLORS`: color sólido del avatar circular de cada empresa.
 *
 * La comparación es case-insensitive y sin espacios extra, para que "GitHub Education" y
 * "github education" caigan en la misma marca. Cualquier empresa no listada usa el indigo
 * de fallback.
 */
fun companyColor(company: String): Color = when (company.trim().lowercase()) {
    "github", "github education" -> Color(0xFF24292E)
    "google" -> Color(0xFF4285F4)
    "jetbrains" -> Color(0xFF087CFA)
    "microsoft azure" -> Color(0xFF0078D4)
    "figma" -> Color(0xFFA259FF)
    "notion" -> Color(0xFF000000)
    "aws" -> Color(0xFFFF9900)
    else -> AppColors.Indigo500
}
