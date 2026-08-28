package com.letmeknow.studentoffers.ui.brand

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `resolveLogoSource` es la cascada de 3 niveles de improve-features.md, verificada como
 * lógica pura (sin Compose ni red).
 */
class LogoSourceTest {

    // --- Nivel 1: Simple Icons ------------------------------------------------------------

    @Test
    fun `empresa de Simple Icons resuelve a SimpleIcon con su color de marca`() {
        val source = resolveLogoSource("GitHub")
        assertTrue(source is LogoSource.SimpleIcon)
        assertEquals(Color(0xFF181717), (source as LogoSource.SimpleIcon).icon.brandColor)
    }

    @Test
    fun `Bolt-new resuelve al icono de StackBlitz`() {
        val source = resolveLogoSource("Bolt.new")
        assertTrue(source is LogoSource.SimpleIcon)
    }

    @Test
    fun `Windsurf resuelve al icono de Codeium`() {
        val source = resolveLogoSource("Windsurf")
        assertTrue(source is LogoSource.SimpleIcon)
    }

    @Test
    fun `Perplexity AI resuelve al slug perplexity`() {
        val source = resolveLogoSource("Perplexity AI")
        assertTrue(source is LogoSource.SimpleIcon)
    }

    @Test
    fun `el matcheo de Simple Icons es case-insensitive y tolera espacios`() {
        assertTrue(resolveLogoSource("github") is LogoSource.SimpleIcon)
        assertTrue(resolveLogoSource("GITHUB") is LogoSource.SimpleIcon)
        assertTrue(resolveLogoSource("  GitHub  ") is LogoSource.SimpleIcon)
    }

    // --- Nivel 2: favicon del dominio real -------------------------------------------------

    @Test
    fun `empresa con dominio curado resuelve a Favicon con la url de Google`() {
        val source = resolveLogoSource("Microsoft")
        assertEquals(
            LogoSource.Favicon("https://www.google.com/s2/favicons?domain=microsoft.com&sz=128"),
            source,
        )
    }

    @Test
    fun `Microsoft Azure y AWS usan sus dominios curados, no el reclaim_link`() {
        assertEquals(
            LogoSource.Favicon("https://www.google.com/s2/favicons?domain=azure.microsoft.com&sz=128"),
            resolveLogoSource("Microsoft Azure"),
        )
        assertEquals(
            LogoSource.Favicon("https://www.google.com/s2/favicons?domain=aws.amazon.com&sz=128"),
            resolveLogoSource("AWS"),
        )
        assertEquals(
            LogoSource.Favicon("https://www.google.com/s2/favicons?domain=aws.amazon.com&sz=128"),
            resolveLogoSource("Amazon Web Services"),
        )
    }

    @Test
    fun `el matcheo de dominios es case-insensitive y tolera espacios`() {
        assertEquals(resolveLogoSource("ibm"), resolveLogoSource("  IBM  "))
    }

    // --- Nivel 3: fallback -------------------------------------------------------------------

    @Test
    fun `empresa desconocida cae en Fallback`() {
        assertEquals(LogoSource.Fallback, resolveLogoSource("Una Empresa Nueva Sin Mapear"))
    }

    @Test
    fun `nombre vacio o solo espacios cae en Fallback`() {
        assertEquals(LogoSource.Fallback, resolveLogoSource(""))
        assertEquals(LogoSource.Fallback, resolveLogoSource("   "))
    }

    // --- Restricción legal: las 7 empresas removidas de Simple Icons -----------------------

    @Test
    fun `ninguna de las 7 empresas restringidas resuelve a SimpleIcon`() {
        val restricted = listOf("Microsoft", "IBM", "Oracle", "Amazon", "AWS", "Adobe", "Runway")

        restricted.forEach { company ->
            val source = resolveLogoSource(company)
            assertTrue(
                "$company no debería resolver a SimpleIcon (logo removido de Simple Icons " +
                    "por pedido legal, ver improve-features.md)",
                source !is LogoSource.SimpleIcon,
            )
            assertTrue(
                "$company debería resolver a Favicon (nivel 2)",
                source is LogoSource.Favicon,
            )
        }
    }
}
