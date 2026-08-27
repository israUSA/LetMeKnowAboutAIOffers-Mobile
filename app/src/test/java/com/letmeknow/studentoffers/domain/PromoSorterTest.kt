package com.letmeknow.studentoffers.domain

import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.fake.FakeClock
import com.letmeknow.studentoffers.fake.NOW
import com.letmeknow.studentoffers.fake.days
import com.letmeknow.studentoffers.fake.promo
import com.letmeknow.studentoffers.feature.promos.PromoTab
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneOffset

class PromoSorterTest {

    private val clock = FakeClock(NOW)
    private val rules = ExpirationRules(clock, ZoneOffset.UTC)

    private val permanente = promo(id = 1, expiresAt = null)
    private val comodo = promo(id = 2, expiresAt = NOW.plusSeconds(days(90)))
    private val urgente = promo(id = 3, expiresAt = NOW.plusSeconds(days(2)))
    private val aviso = promo(id = 4, expiresAt = NOW.plusSeconds(days(20)))

    @Test
    fun `ordena urgent warning comfortable permanent`() {
        val desordenado = listOf(permanente, comodo, urgente, aviso)

        assertEquals(
            listOf(urgente, aviso, comodo, permanente),
            PromoSorter.sortByUrgency(desordenado, rules),
        )
    }

    @Test
    fun `el orden es estable dentro de un mismo estado`() {
        val primerUrgente = promo(id = 10, expiresAt = NOW.plusSeconds(days(1)))
        val segundoUrgente = promo(id = 11, expiresAt = NOW.plusSeconds(days(7)))

        // Aunque el segundo vence más tarde, no se reordena: el criterio es el estado, no la
        // fecha, y `sortedBy` conserva el orden de entrada dentro del mismo estado.
        assertEquals(
            listOf(primerUrgente, segundoUrgente, aviso),
            PromoSorter.sortByUrgency(listOf(primerUrgente, segundoUrgente, aviso), rules),
        )
    }

    @Test
    fun `el orden se recalcula con la hora, no con el dato`() {
        val lista = listOf(comodo, aviso)
        assertEquals(listOf(aviso, comodo), PromoSorter.sortByUrgency(lista, rules))

        // Pasan 85 días: la que era cómoda ahora es la urgente y pasa al frente.
        clock.instant = NOW.plusSeconds(days(85))
        assertEquals(listOf(comodo, aviso), PromoSorter.sortByUrgency(lista, rules))
    }

    @Test
    fun `el orden se aplica sobre la lista ya buscada y filtrada`() {
        val catalogo = listOf(permanente, comodo, urgente, aviso)
        val visible = PromoFilter.apply(catalogo, query = "", tab = PromoTab.LIMITED)

        assertEquals(
            listOf(urgente, aviso, comodo),
            PromoSorter.sortByUrgency(visible, rules),
        )
    }

    @Test
    fun `la sobrecarga con estado ya calculado ordena igual`() {
        val items = listOf(
            "permanente" to ExpirationState.PERMANENT,
            "urgente" to ExpirationState.URGENT,
            "comodo" to ExpirationState.COMFORTABLE,
            "aviso" to ExpirationState.WARNING,
        )

        assertEquals(
            listOf("urgente", "aviso", "comodo", "permanente"),
            PromoSorter.sortByUrgency(items) { it.second }.map { it.first },
        )
    }
}
