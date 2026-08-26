package com.letmeknow.aioffers.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Medidas compartidas. Las mantiene el coordinador; los agentes las consumen. */
object Dimens {

    /**
     * Piso mínimo de área táctil para todo elemento interactivo: tabs de filtro, botón de
     * campana del header, CTA "Reclamar", chevron. Es un estándar de accesibilidad, no una
     * particularidad de la web.
     */
    val MinTouchTarget = 44.dp

    /** Ancho máximo del contenido, equivalente a `max-w-7xl` (~1280px) en pantallas grandes. */
    val MaxContentWidth = 1280.dp

    /** Umbral para pasar de 1 a 2 columnas en la grilla (tablets y plegables abiertos). */
    val TwoColumnBreakpoint = 600.dp

    /** Umbral para pasar a 3 columnas. */
    val ThreeColumnBreakpoint = 900.dp

    val ScreenPadding = 16.dp
    val SectionSpacing = 24.dp
    val CardPadding = 16.dp
    val CardSpacing = 12.dp

    /** Barra de progreso de tiempo: "muy fina, arriba del todo" de la tarjeta. */
    val ProgressBarHeight = 3.dp

    /** Avatar circular con la inicial de la empresa. */
    val CompanyAvatarSize = 40.dp

    // Esquinas: `rounded-2xl` = 16dp, `rounded-3xl` = 24dp.
    val Shape2xl = RoundedCornerShape(16.dp)
    val Shape3xl = RoundedCornerShape(24.dp)
    val ShapePill = RoundedCornerShape(percent = 50)
}
