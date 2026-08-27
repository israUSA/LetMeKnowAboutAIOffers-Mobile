package com.letmeknow.studentoffers.ui.theme

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

/**
 * Glassmorphism.
 *
 * En Android no existe `backdrop-filter`: `Modifier.blur` difumina el propio composable, no
 * lo que está detrás. haze resuelve el blur real de fondo, y este archivo es el ÚNICO lugar
 * del proyecto que lo toca — si más adelante hay que cambiar de implementación (ver el
 * checkpoint de rendimiento del plan), se cambia acá y nada más.
 *
 * Uso: `Modifier.glassSource(hazeState)` sobre el fondo aurora, y `Modifier.glassSurface(...)`
 * sobre cada superficie traslúcida (header, tarjetas, buscador, pills, estados).
 */
object Glass {

    /** Blur fuerte, equivalente a `backdrop-blur-xl` de Tailwind. */
    val BlurRadius = 24.dp

    /** Grano sutil: evita el banding que deja un blur grande sobre un fondo casi negro. */
    const val NoiseFactor = 0.05f

    fun style(tint: Color): HazeStyle = HazeStyle(
        backgroundColor = AppColors.Background,
        tints = listOf(HazeTint(tint)),
        blurRadius = BlurRadius,
        noiseFactor = NoiseFactor,
    )
}

/** Va sobre el contenido que debe verse difuminado detrás del vidrio (el fondo aurora). */
fun Modifier.glassSource(state: HazeState): Modifier = hazeSource(state)

/**
 * Superficie de vidrio: blur del fondo + tinte blanco a baja opacidad + borde translúcido.
 *
 * @param tint blanco entre 4% y 9% — usar las constantes `AppColors.GlassSurface*`.
 * @param border blanco entre 10% y 25% — usar `AppColors.GlassBorder*`.
 */
fun Modifier.glassSurface(
    state: HazeState,
    shape: Shape = Dimens.Shape2xl,
    tint: Color = AppColors.GlassSurfaceMid,
    border: Color = AppColors.GlassBorderLow,
): Modifier = this
    .clip(shape)
    .hazeEffect(state = state, style = Glass.style(tint))
    .border(width = 1.dp, color = border, shape = shape)
