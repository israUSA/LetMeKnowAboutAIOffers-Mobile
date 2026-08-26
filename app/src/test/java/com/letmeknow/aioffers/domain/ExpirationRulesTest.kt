package com.letmeknow.aioffers.domain

import com.letmeknow.aioffers.domain.model.ExpirationState
import com.letmeknow.aioffers.fake.FakeClock
import com.letmeknow.aioffers.fake.NOW
import com.letmeknow.aioffers.fake.days
import com.letmeknow.aioffers.fake.promo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

/**
 * Reglas de DATA_AND_API.md, verificadas en los bordes exactos.
 *
 * La zona horaria se fija en UTC para que la fecha absoluta ("Expira el 15 mar 2027") no
 * dependa de la máquina que corra el test.
 */
class ExpirationRulesTest {

    private val clock = FakeClock(NOW)
    private val rules = ExpirationRules(clock, ZoneOffset.UTC)

    private fun stateIn(seconds: Long): ExpirationState =
        rules.getExpirationState(NOW.plusSeconds(seconds))

    // --- getExpirationState -------------------------------------------------------------

    @Test
    fun `expiresAt nulo es permanente`() {
        assertEquals(ExpirationState.PERMANENT, rules.getExpirationState(null as Instant?))
        assertEquals(ExpirationState.PERMANENT, rules.getExpirationState(promo(expiresAt = null)))
    }

    @Test
    fun `cero dias es urgente`() {
        assertEquals(ExpirationState.URGENT, stateIn(0))
    }

    @Test
    fun `un dia es urgente`() {
        assertEquals(ExpirationState.URGENT, stateIn(days(1)))
    }

    @Test
    fun `siete dias exactos siguen siendo urgente`() {
        assertEquals(ExpirationState.URGENT, stateIn(days(7)))
    }

    @Test
    fun `un segundo despues de siete dias ya es warning`() {
        // ceil() redondea hacia arriba: 7 días + 1 segundo son 8 días.
        assertEquals(ExpirationState.WARNING, stateIn(days(7) + 1))
    }

    @Test
    fun `ocho dias es warning`() {
        assertEquals(ExpirationState.WARNING, stateIn(days(8)))
    }

    @Test
    fun `treinta dias exactos siguen siendo warning`() {
        assertEquals(ExpirationState.WARNING, stateIn(days(30)))
    }

    @Test
    fun `un segundo despues de treinta dias ya es comfortable`() {
        assertEquals(ExpirationState.COMFORTABLE, stateIn(days(30) + 1))
    }

    @Test
    fun `treinta y un dias es comfortable`() {
        assertEquals(ExpirationState.COMFORTABLE, stateIn(days(31)))
    }

    @Test
    fun `una oferta vencida sigue clasificando como urgente`() {
        // days negativos entran por `days <= 7`. La web se comporta igual: una oferta vencida
        // no desaparece de la grilla, aparece primera con el texto "Expirada".
        assertEquals(ExpirationState.URGENT, stateIn(-days(1)))
        assertEquals(ExpirationState.URGENT, stateIn(-days(400)))
    }

    // --- daysUntil ----------------------------------------------------------------------

    @Test
    fun `daysUntil redondea hacia arriba`() {
        assertEquals(1L, rules.daysUntil(NOW.plusSeconds(1)))
        assertEquals(3L, rules.daysUntil(NOW.plusSeconds(days(2) + 1)))
    }

    @Test
    fun `daysUntil corta hacia cero en el pasado`() {
        // ceil(-0,5) es -0: menos de un día vencida todavía cuenta como 0.
        assertEquals(0L, rules.daysUntil(NOW.minusSeconds(days(1) / 2)))
        assertEquals(-1L, rules.daysUntil(NOW.minusSeconds(days(1))))
        assertEquals(-2L, rules.daysUntil(NOW.minusSeconds(days(2))))
    }

    // --- formatRelativeDate -------------------------------------------------------------

    @Test
    fun `permanente dice siempre disponible`() {
        assertEquals("Siempre disponible", rules.formatRelativeDate(promo(expiresAt = null)))
    }

    @Test
    fun `dias negativos dicen expirada`() {
        assertEquals("Expirada", rules.formatRelativeDate(NOW.minusSeconds(days(1))))
        assertEquals("Expirada", rules.formatRelativeDate(NOW.minusSeconds(days(10))))
    }

    @Test
    fun `cero dias dice expira hoy`() {
        assertEquals("Expira hoy", rules.formatRelativeDate(NOW))
    }

    @Test
    fun `un dia dice expira manana`() {
        assertEquals("Expira mañana", rules.formatRelativeDate(NOW.plusSeconds(days(1))))
    }

    @Test
    fun `hasta treinta dias dice expira en N dias`() {
        assertEquals("Expira en 2 días", rules.formatRelativeDate(NOW.plusSeconds(days(2))))
        assertEquals("Expira en 30 días", rules.formatRelativeDate(NOW.plusSeconds(days(30))))
    }

    @Test
    fun `mas de treinta dias dice la fecha absoluta en es-ES`() {
        val marzo2027 = Instant.parse("2027-03-15T09:00:00Z")
        assertEquals("Expira el 15 mar 2027", rules.formatRelativeDate(marzo2027))
    }

    @Test
    fun `la etiqueta queda vacia cuando el estado es urgente`() {
        // El badge se reemplaza por el countdown en vivo, así que no hay texto estático.
        assertEquals("", rules.expirationLabel(promo(expiresAt = NOW.plusSeconds(days(3)))))
        assertEquals(
            "Expira en 10 días",
            rules.expirationLabel(promo(expiresAt = NOW.plusSeconds(days(10)))),
        )
    }

    // --- getTimeRemainingPercent --------------------------------------------------------

    @Test
    fun `una oferta permanente no tiene porcentaje`() {
        assertNull(rules.getTimeRemainingPercent(promo(expiresAt = null)))
    }

    @Test
    fun `la mitad de la ventana da cincuenta por ciento`() {
        val subject = promo(
            createdAt = NOW.minusSeconds(days(5)),
            expiresAt = NOW.plusSeconds(days(5)),
        )
        assertEquals(50f, rules.getTimeRemainingPercent(subject)!!, 0.001f)
    }

    @Test
    fun `clampea en cien cuando ahora es anterior a createdAt`() {
        val subject = promo(
            createdAt = NOW.plusSeconds(days(1)),
            expiresAt = NOW.plusSeconds(days(11)),
        )
        assertEquals(100f, rules.getTimeRemainingPercent(subject)!!, 0.001f)
    }

    @Test
    fun `clampea en cero cuando la oferta ya vencio`() {
        val subject = promo(
            createdAt = NOW.minusSeconds(days(20)),
            expiresAt = NOW.minusSeconds(days(5)),
        )
        assertEquals(0f, rules.getTimeRemainingPercent(subject)!!, 0.001f)
    }

    @Test
    fun `una ventana de duracion cero no divide por cero`() {
        val subject = promo(createdAt = NOW, expiresAt = NOW)
        assertEquals(0f, rules.getTimeRemainingPercent(subject)!!, 0.001f)
    }

    // --- countdownTo --------------------------------------------------------------------

    @Test
    fun `el countdown descompone el tiempo restante`() {
        val target = NOW.plusSeconds(days(1) + 2 * 3600 + 3 * 60 + 4)
        assertEquals(Countdown(days = 1, hours = 2, minutes = 3, seconds = 4), rules.countdownTo(target))
    }

    @Test
    fun `el countdown no muestra numeros negativos`() {
        assertEquals(Countdown.Zero, rules.countdownTo(NOW.minusSeconds(1)))
        assertEquals(Countdown.Zero, rules.countdownTo(NOW))
    }

    @Test
    fun `mover el reloj cambia el estado sin tocar la oferta`() {
        val subject = promo(expiresAt = NOW.plusSeconds(days(31)))
        assertEquals(ExpirationState.COMFORTABLE, rules.getExpirationState(subject))

        clock.instant = NOW.plusSeconds(days(25))
        assertEquals(ExpirationState.URGENT, rules.getExpirationState(subject))
    }
}
