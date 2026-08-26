package com.letmeknow.aioffers.domain

import com.letmeknow.aioffers.fake.NOW
import com.letmeknow.aioffers.fake.days
import com.letmeknow.aioffers.fake.promo
import com.letmeknow.aioffers.feature.promos.PromoTab
import com.letmeknow.aioffers.feature.promos.TabCounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromoFilterTest {

    private val github = promo(
        id = 1,
        company = "GitHub",
        title = "Student Developer Pack",
        description = "Incluye créditos de computación cuántica y dominios gratis.",
        expiresAt = null,
    )
    private val figma = promo(
        id = 2,
        company = "Figma",
        title = "Plan Education",
        description = "Archivos ilimitados para estudiantes.",
        expiresAt = NOW.plusSeconds(days(3)),
    )
    private val jetbrains = promo(
        id = 3,
        company = "JetBrains",
        title = "Licencia académica",
        description = "Todas las IDE, renovable cada año.",
        expiresAt = NOW.plusSeconds(days(200)),
    )

    private val catalog = listOf(github, figma, jetbrains)

    // --- Búsqueda -----------------------------------------------------------------------

    @Test
    fun `la busqueda es case-insensitive sobre company`() {
        assertEquals(listOf(github), PromoFilter.search(catalog, "github"))
        assertEquals(listOf(github), PromoFilter.search(catalog, "GITHUB"))
        assertEquals(listOf(github), PromoFilter.search(catalog, "GiThUb"))
    }

    @Test
    fun `la busqueda es case-insensitive sobre title`() {
        assertEquals(listOf(figma), PromoFilter.search(catalog, "plan education"))
        assertEquals(listOf(jetbrains), PromoFilter.search(catalog, "ACADÉMICA"))
    }

    @Test
    fun `la busqueda NO mira description`() {
        // "cuántica" solo aparece en la descripción de GitHub. Buscarlo no debe devolver nada:
        // la descripción está oculta hasta expandir la tarjeta, y un match invisible parece
        // un resultado equivocado.
        assertTrue(PromoFilter.search(catalog, "cuántica").isEmpty())
        assertTrue(PromoFilter.search(catalog, "ilimitados").isEmpty())
        assertTrue(PromoFilter.search(catalog, "renovable").isEmpty())
    }

    @Test
    fun `una busqueda vacia o en blanco no filtra`() {
        assertEquals(catalog, PromoFilter.search(catalog, ""))
        assertEquals(catalog, PromoFilter.search(catalog, "   "))
    }

    @Test
    fun `la busqueda matchea subcadenas parciales`() {
        assertEquals(listOf(github), PromoFilter.search(catalog, "hub"))
    }

    // --- Tabs ---------------------------------------------------------------------------

    @Test
    fun `el tab ALL no filtra`() {
        assertEquals(catalog, PromoFilter.byTab(catalog, PromoTab.ALL))
    }

    @Test
    fun `el tab PERMANENT solo deja las que no vencen`() {
        assertEquals(listOf(github), PromoFilter.byTab(catalog, PromoTab.PERMANENT))
    }

    @Test
    fun `el tab LIMITED solo deja las que vencen`() {
        assertEquals(listOf(figma, jetbrains), PromoFilter.byTab(catalog, PromoTab.LIMITED))
    }

    // --- Contadores ---------------------------------------------------------------------

    @Test
    fun `los contadores salen de la lista completa`() {
        assertEquals(TabCounts(all = 3, permanent = 1, limited = 2), PromoFilter.counts(catalog))
    }

    @Test
    fun `los contadores no cambian con la busqueda ni con el tab activo`() {
        val esperado = TabCounts(all = 3, permanent = 1, limited = 2)

        // Lo que ve el usuario se reduce a una oferta...
        val visible = PromoFilter.apply(catalog, query = "figma", tab = PromoTab.LIMITED)
        assertEquals(listOf(figma), visible)

        // ...pero los contadores se siguen calculando sobre el catálogo completo.
        assertEquals(esperado, PromoFilter.counts(catalog))
    }

    @Test
    fun `un catalogo vacio da contadores en cero`() {
        assertEquals(TabCounts(all = 0, permanent = 0, limited = 0), PromoFilter.counts(emptyList()))
    }

    // --- Composición --------------------------------------------------------------------

    @Test
    fun `apply combina busqueda y tab`() {
        assertEquals(emptyList<Any>(), PromoFilter.apply(catalog, "github", PromoTab.LIMITED))
        assertEquals(listOf(github), PromoFilter.apply(catalog, "github", PromoTab.PERMANENT))
        assertEquals(listOf(figma, jetbrains), PromoFilter.apply(catalog, "", PromoTab.LIMITED))
    }
}
