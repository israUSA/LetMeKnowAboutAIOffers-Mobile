package com.letmeknow.studentoffers.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.letmeknow.studentoffers.core.ui.LocalReduceMotion
import com.letmeknow.studentoffers.core.ui.isReduceMotionEnabled

/**
 * El diseño es exclusivamente oscuro y no hay toggle de tema, así que el esquema es fijo:
 * no se consulta [isSystemInDarkTheme] ni se usa color dinámico de Material You. Forzarlo
 * es intencional, no un olvido.
 */
private val AppColorScheme = darkColorScheme(
    primary = AppColors.Indigo500,
    secondary = AppColors.Fuchsia500,
    background = AppColors.Background,
    surface = AppColors.Background,
    onPrimary = AppColors.OnBackground,
    onSecondary = AppColors.OnBackground,
    onBackground = AppColors.OnBackground,
    onSurface = AppColors.OnBackground,
    onSurfaceVariant = AppColors.OnBackgroundMuted,
)

/** Gradiente de marca sólido: indigo → fucsia. Ícono del logo, tab activo, botón "Reclamar". */
val BrandGradient: Brush
    get() = Brush.horizontalGradient(listOf(AppColors.Indigo500, AppColors.Fuchsia500))

/**
 * Gradiente de texto (~100deg): indigo → fucsia → cian, con la parada del medio al 45%.
 * Se aplica con `TextStyle(brush = TextGradient)`, que es el equivalente Compose de
 * `background-clip: text`.
 */
val TextGradient: Brush
    get() = Brush.linearGradient(
        colorStops = arrayOf(
            0f to AppColors.TextGradientStart,
            0.45f to AppColors.TextGradientMid,
            1f to AppColors.TextGradientEnd,
        ),
    )

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    // Se lee una vez por composición de la raíz: no cambia mientras la app está en pantalla,
    // y leerlo en cada animación sería un acceso a Settings por frame.
    val reduceMotion = remember(context) { context.isReduceMotionEnabled() }

    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
