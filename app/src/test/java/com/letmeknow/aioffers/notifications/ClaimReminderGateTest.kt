package com.letmeknow.aioffers.notifications

import com.letmeknow.aioffers.fake.FakeClock
import com.letmeknow.aioffers.fake.FakePromoRepository
import com.letmeknow.aioffers.fake.NOW
import com.letmeknow.aioffers.fake.days
import com.letmeknow.aioffers.fake.promo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * La reverificación, que es la razón de ser del worker de recordatorios.
 *
 * Entre programar un aviso y dispararlo pasan días. Estos tests fijan las cinco maneras en que
 * el estado puede haber cambiado en el medio y exigen que ninguna termine en una notificación:
 * avisar de algo que ya no es cierto es peor que no avisar.
 */
class ClaimReminderGateTest {

    private val clock = FakeClock(NOW)

    private val vigente = promo(id = 1L, expiresAt = NOW.plusSeconds(days(2)))

    private fun gate(
        promos: List<com.letmeknow.aioffers.domain.model.Promo> = listOf(vigente),
        followed: Set<Long> = setOf(1L),
        claimed: Set<Long> = emptySet(),
    ) = ClaimReminderGate(
        repository = FakePromoRepository(promos = promos, followed = followed, claimed = claimed),
        clock = clock,
    )

    @Test
    fun `vigente y sin reclamar si notifica`() = runTest {
        assertEquals(vigente, gate().promoToNotify(1L))
    }

    @Test
    fun `ya reclamada no notifica`() = runTest {
        assertNull(gate(claimed = setOf(1L)).promoToNotify(1L))
    }

    @Test
    fun `ya vencida no notifica`() = runTest {
        val vencida = promo(id = 1L, expiresAt = NOW.minusSeconds(days(1)))

        assertNull(gate(promos = listOf(vencida)).promoToNotify(1L))
    }

    @Test
    fun `dejada de seguir no notifica`() = runTest {
        assertNull(gate(followed = emptySet()).promoToNotify(1L))
    }

    @Test
    fun `desaparecida del catalogo no notifica`() = runTest {
        assertNull(gate(promos = emptyList()).promoToNotify(1L))
    }

    /**
     * Una permanente no debería tener recordatorio programado nunca (ver
     * [ClaimReminderPlannerTest]), pero si uno viejo sobrevivió a que el backend le sacara la
     * fecha de vencimiento, la reverificación es la última línea que impide el aviso.
     */
    @Test
    fun `permanente no notifica aunque tenga work pendiente`() = runTest {
        val permanente = promo(id = 1L, expiresAt = null)

        assertNull(gate(promos = listOf(permanente)).promoToNotify(1L))
    }

    /** El instante exacto del vencimiento ya no es "vigente": el aviso llega tarde. */
    @Test
    fun `justo en el instante del vencimiento no notifica`() = runTest {
        val justoAhora = promo(id = 1L, expiresAt = NOW)

        assertNull(gate(promos = listOf(justoAhora)).promoToNotify(1L))
    }
}
