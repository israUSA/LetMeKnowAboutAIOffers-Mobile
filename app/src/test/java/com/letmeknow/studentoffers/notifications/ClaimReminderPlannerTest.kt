package com.letmeknow.studentoffers.notifications

import com.letmeknow.studentoffers.fake.NOW
import com.letmeknow.studentoffers.fake.days
import com.letmeknow.studentoffers.fake.promo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

/**
 * La regla acordada: dos avisos por oferta, a 3 días y a 1 día del vencimiento, y ninguno para
 * las permanentes.
 *
 * Que el plan venga incompleto no es un error a tapar: es la señal de que `DefaultNotifier`
 * tiene que cancelar el work del momento ausente en vez de dejarlo huérfano.
 */
class ClaimReminderPlannerTest {

    @Test
    fun `permanente no genera ningun aviso`() {
        val plan = ClaimReminderPlanner.planFor(promo(expiresAt = null), NOW)

        assertTrue(plan.isEmpty())
    }

    @Test
    fun `con vencimiento lejano programa los dos avisos`() {
        val plan = ClaimReminderPlanner.planFor(promo(expiresAt = NOW.plusSeconds(days(10))), NOW)

        assertEquals(
            listOf(ClaimReminderOffset.THREE_DAYS, ClaimReminderOffset.ONE_DAY),
            plan.map { it.offset },
        )
        // 10 días de vencimiento menos 3 y menos 1: los delays son los que faltan hasta cada
        // momento, no hasta el vencimiento.
        assertEquals(Duration.ofDays(7), plan[0].delay)
        assertEquals(Duration.ofDays(9), plan[1].delay)
    }

    @Test
    fun `si el momento de 3 dias ya paso solo queda el de 1 dia`() {
        val plan = ClaimReminderPlanner.planFor(promo(expiresAt = NOW.plusSeconds(days(2))), NOW)

        assertEquals(listOf(ClaimReminderOffset.ONE_DAY), plan.map { it.offset })
        assertEquals(Duration.ofDays(1), plan.single().delay)
    }

    @Test
    fun `a menos de un dia del vencimiento no queda ningun aviso`() {
        val plan = ClaimReminderPlanner.planFor(
            promo(expiresAt = NOW.plusSeconds(days(1) / 2)),
            NOW,
        )

        assertTrue(plan.isEmpty())
    }

    @Test
    fun `una oferta ya vencida no genera avisos`() {
        val plan = ClaimReminderPlanner.planFor(promo(expiresAt = NOW.minusSeconds(days(1))), NOW)

        assertTrue(plan.isEmpty())
    }

    /**
     * Los nombres son el contrato de unicidad: reprogramar la misma oferta tiene que caer en
     * el mismo work único para que `ExistingWorkPolicy.REPLACE` reemplace en vez de duplicar.
     */
    @Test
    fun `los nombres de work son estables y distintos por momento`() {
        assertEquals("claim-42-3d", ClaimReminderOffset.THREE_DAYS.workName(42L))
        assertEquals("claim-42-1d", ClaimReminderOffset.ONE_DAY.workName(42L))
    }
}
