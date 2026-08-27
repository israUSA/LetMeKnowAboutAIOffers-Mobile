package com.letmeknow.studentoffers.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta traducida literalmente desde DESIGN_SYSTEM.md.
 *
 * El diseño es exclusivamente oscuro y no hay toggle de tema, así que no existe una
 * variante clara de estos valores a propósito.
 *
 * Este archivo lo mantiene el coordinador. Los agentes de worktree lo consumen, no lo editan.
 */
object AppColors {

    // --- Base ---
    /** Fondo base: casi negro con tinte azul/violeta. */
    val Background = Color(0xFF060610)

    /** Color general del texto. */
    val OnBackground = Color(0xFFE2E8F0)

    /** Texto secundario, para subtítulos y metadatos. */
    val OnBackgroundMuted = Color(0xFF94A3B8)

    // --- Aurora (tres manchas radiales del fondo fijo) ---
    val AuroraIndigo = Color(0x4D4F46E5)   // rgba(79, 70, 229, .30)
    val AuroraFuchsia = Color(0x40C026D3)  // rgba(192, 38, 211, .25)
    val AuroraCyan = Color(0x3306B6D4)     // rgba(6, 182, 212, .20)

    /** Viñeta que apaga los blobs hacia los bordes: transparente al centro, casi opaca afuera. */
    val AuroraVignetteEdge = Color(0xD9060610) // rgba(6, 6, 16, .85)

    // --- Glassmorphism ---
    // Superficies traslúcidas: blanco entre 4% y 9%, borde blanco entre 10% y 25%.
    val GlassSurfaceLow = Color(0x0AFFFFFF)   // white 4%
    val GlassSurfaceMid = Color(0x0FFFFFFF)   // white 6%
    val GlassSurfaceHigh = Color(0x17FFFFFF)  // white 9%
    val GlassBorderLow = Color(0x1AFFFFFF)    // white 10%
    val GlassBorderMid = Color(0x2EFFFFFF)    // white 18%
    val GlassBorderHigh = Color(0x40FFFFFF)   // white 25%

    // --- Acentos de marca ---
    val Indigo500 = Color(0xFF6366F1)
    val Fuchsia500 = Color(0xFFD946EF)

    /** Paradas del gradiente de texto (~100deg): indigo → fucsia → cian. */
    val TextGradientStart = Color(0xFFA5B4FC)
    val TextGradientMid = Color(0xFFE879F9)    // parada al 45%
    val TextGradientEnd = Color(0xFF67E8F9)

    // --- Colores por estado de expiración ---
    val Emerald300 = Color(0xFF6EE7B7)
    val Emerald400 = Color(0xFF34D399)
    val Teal500 = Color(0xFF14B8A6)

    val Sky300 = Color(0xFF7DD3FC)
    val Sky400 = Color(0xFF38BDF8)
    val Cyan500 = Color(0xFF06B6D4)

    val Amber300 = Color(0xFFFCD34D)
    val Amber400 = Color(0xFFFBBF24)
    val Orange500 = Color(0xFFF97316)

    val Rose300 = Color(0xFFFDA4AF)
    val Rose400 = Color(0xFFFB7185)
    val Red500 = Color(0xFFEF4444)
}
