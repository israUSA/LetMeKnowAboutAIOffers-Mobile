package com.letmeknow.studentoffers.notifications

import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.fake.FakePromoRepository
import com.letmeknow.studentoffers.fake.RecordingNotifier
import com.letmeknow.studentoffers.fake.promo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La detección de ofertas nuevas: sin backend que avise, lo único disponible es refrescar el
 * catálogo cada tanto y comparar ids contra lo que ya había en Room.
 */
class NewPromoDetectorTest {

    private val viejas = listOf(promo(id = 1L), promo(id = 2L))

    @Test
    fun `detecta los ids que no estaban`() {
        val refrescadas = viejas + promo(id = 3L) + promo(id = 4L)

        val nuevas = NewPromoDetector.newPromos(setOf(1L, 2L), refrescadas)

        assertEquals(listOf(3L, 4L), nuevas.map(Promo::id))
    }

    @Test
    fun `sin ids nuevos no devuelve nada`() {
        assertTrue(NewPromoDetector.newPromos(setOf(1L, 2L), viejas).isEmpty())
    }

    /**
     * En la primera corrida el caché está vacío y no hay contra qué comparar. Avisar de "20
     * ofertas nuevas" apenas instalada la app sería ruido, no una novedad.
     */
    @Test
    fun `con el cache vacio no avisa de nada`() {
        assertTrue(NewPromoDetector.newPromos(emptySet(), viejas).isEmpty())
    }

    /** Una oferta que el backend dio de baja no es una novedad, y no debe contarse como tal. */
    @Test
    fun `una oferta que desaparecio no cuenta como nueva`() {
        val refrescadas = listOf(promo(id = 2L), promo(id = 3L))

        val nuevas = NewPromoDetector.newPromos(setOf(1L, 2L), refrescadas)

        assertEquals(listOf(3L), nuevas.map(Promo::id))
    }

    /**
     * La secuencia completa que corre `CatalogRefreshWorker`: leer los ids cacheados, refrescar
     * y recién entonces comparar. Si se leyera el caché **después** del refresh, la comparación
     * sería contra sí misma y no habría novedad nunca.
     */
    @Test
    fun `la secuencia del worker avisa solo de lo que llego nuevo`() = runTest {
        val repository = FakePromoRepository(
            promos = viejas,
            refreshedPromos = viejas + promo(id = 3L),
        )
        val notifier = RecordingNotifier()

        val conocidas = repository.promos.first().mapTo(mutableSetOf(), Promo::id)
        repository.refresh()
        notifier.notifyNewPromos(
            NewPromoDetector.newPromos(conocidas, repository.promos.first()),
        )

        assertEquals(listOf(listOf(3L)), notifier.notified.map { batch -> batch.map(Promo::id) })
    }
}
