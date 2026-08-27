package com.letmeknow.studentoffers.domain

import com.letmeknow.studentoffers.core.time.Clock
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.math.ceil

/** Días, horas, minutos y segundos que faltan. Nunca negativos: ver [ExpirationRules.countdownTo]. */
data class Countdown(val days: Long, val hours: Long, val minutes: Long, val seconds: Long) {
    companion object {
        val Zero = Countdown(0, 0, 0, 0)
    }
}

/**
 * Reglas de vencimiento, exactamente como las define DATA_AND_API.md. Kotlin puro, sin
 * Compose y sin `Instant.now()`: el reloj entra por constructor para que los umbrales de 7 y
 * 30 días se puedan verificar fijando la hora en vez de esperándola.
 */
class ExpirationRules(
    private val clock: Clock,
    private val zone: ZoneId = ZoneId.systemDefault(),
) {

    /**
     * `ceil((expiresAt - ahora) / 1 día)`, igual que la web.
     *
     * El redondeo hacia arriba tiene una consecuencia deliberada en el pasado: `ceil` corta
     * hacia cero, así que una oferta vencida hace 12 horas da `0` (no `-1`) y se muestra como
     * "Expira hoy". Es el comportamiento de la web y se replica tal cual; recién a partir de
     * un día completo vencida el resultado es negativo y el texto pasa a "Expirada".
     */
    fun daysUntil(expiresAt: Instant): Long {
        val remainingMillis = Duration.between(clock.now(), expiresAt).toMillis()
        return ceil(remainingMillis.toDouble() / MILLIS_PER_DAY).toLong()
    }

    fun getExpirationState(promo: Promo): ExpirationState = getExpirationState(promo.expiresAt)

    /** `null` significa oferta permanente; el resto son umbrales en días. */
    fun getExpirationState(expiresAt: Instant?): ExpirationState {
        expiresAt ?: return ExpirationState.PERMANENT

        val days = daysUntil(expiresAt)
        return when {
            days <= URGENT_DAYS -> ExpirationState.URGENT
            days <= WARNING_DAYS -> ExpirationState.WARNING
            else -> ExpirationState.COMFORTABLE
        }
    }

    /**
     * Texto de expiración de una oferta. Una permanente no calcula fecha: devuelve
     * "Siempre disponible".
     */
    fun formatRelativeDate(promo: Promo): String = formatRelativeDate(promo.expiresAt)

    fun formatRelativeDate(expiresAt: Instant?): String {
        expiresAt ?: return PERMANENT_LABEL

        val days = daysUntil(expiresAt)
        return when {
            days < 0 -> "Expirada"
            days == 0L -> "Expira hoy"
            days == 1L -> "Expira mañana"
            days <= WARNING_DAYS -> "Expira en $days días"
            else -> "Expira el ${ABSOLUTE_DATE.format(expiresAt.atZone(zone))}"
        }
    }

    /**
     * Etiqueta lista para pintar en la tarjeta.
     *
     * Vacía cuando el estado es [ExpirationState.URGENT] porque ahí el badge se reemplaza por
     * el countdown en vivo ([countdownTo]), no por texto estático (DESIGN_SYSTEM.md).
     */
    fun expirationLabel(promo: Promo): String = when (getExpirationState(promo)) {
        ExpirationState.URGENT -> ""
        else -> formatRelativeDate(promo)
    }

    /**
     * Porcentaje de tiempo que **queda**, 0..100, para la barra de progreso.
     *
     * `null` si la oferta es permanente: no hay total contra el cual medir. Si la ventana
     * `createdAt..expiresAt` es de duración cero o negativa (dato inconsistente del backend)
     * devuelve 0 en vez de dividir por cero.
     */
    fun getTimeRemainingPercent(promo: Promo): Float? {
        val expiresAt = promo.expiresAt ?: return null

        val totalMillis = Duration.between(promo.createdAt, expiresAt).toMillis()
        if (totalMillis <= 0L) return 0f

        val remainingMillis = Duration.between(clock.now(), expiresAt).toMillis()
        return (remainingMillis.toDouble() / totalMillis * 100).coerceIn(0.0, 100.0).toFloat()
    }

    /**
     * Días/horas/minutos/segundos hasta [expiresAt], recalculado por el ticker de la pantalla.
     *
     * Si la fecha ya pasó devuelve [Countdown.Zero] en vez de números negativos, que es lo que
     * pide DATA_AND_API.md.
     */
    fun countdownTo(expiresAt: Instant): Countdown {
        val remaining = Duration.between(clock.now(), expiresAt)
        if (remaining.isNegative || remaining.isZero) return Countdown.Zero

        return Countdown(
            days = remaining.toDays(),
            hours = remaining.toHours() % 24,
            minutes = remaining.toMinutes() % 60,
            seconds = remaining.seconds % 60,
        )
    }

    companion object {
        const val PERMANENT_LABEL = "Siempre disponible"

        private const val URGENT_DAYS = 7L
        private const val WARNING_DAYS = 30L
        private const val MILLIS_PER_DAY = 86_400_000.0

        private val SPANISH = Locale.forLanguageTag("es-ES")

        /**
         * Abreviaturas de mes fijadas a mano en vez de delegarlas al locale del sistema.
         *
         * DATA_AND_API.md especifica el resultado exacto ("Expira el 15 mar 2027") y las
         * abreviaturas de es-ES cambiaron entre versiones de CLDR (con punto, sin punto,
         * "sep" vs "sept"). Fijarlas es lo único que hace que el texto sea el mismo en
         * cualquier dispositivo y que el test no dependa del JDK que corra.
         */
        private val SPANISH_SHORT_MONTHS = mapOf(
            1L to "ene", 2L to "feb", 3L to "mar", 4L to "abr", 5L to "may", 6L to "jun",
            7L to "jul", 8L to "ago", 9L to "sep", 10L to "oct", 11L to "nov", 12L to "dic",
        )

        private val ABSOLUTE_DATE: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(' ')
            .appendText(ChronoField.MONTH_OF_YEAR, SPANISH_SHORT_MONTHS)
            .appendLiteral(' ')
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter(SPANISH)
    }
}
