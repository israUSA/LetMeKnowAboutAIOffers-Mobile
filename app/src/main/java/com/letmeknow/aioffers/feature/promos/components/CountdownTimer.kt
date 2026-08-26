package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.letmeknow.aioffers.core.ui.LocalReduceMotion
import com.letmeknow.aioffers.domain.model.ExpirationState
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.Motion
import com.letmeknow.aioffers.ui.theme.colors

private const val SecondsPerMinute = 60L
private const val SecondsPerHour = 60L * SecondsPerMinute
private const val SecondsPerDay = 24L * SecondsPerHour

/**
 * Countdown en vivo del estado [ExpirationState.URGENT]. Reemplaza al [ExpirationBadge], no se
 * suma a él, así que reutiliza el mismo chrome ([StatePill]) con los colores de `urgent`.
 *
 * @param remainingSeconds segundos que faltan hasta `expires_at`, **ya calculados**. El tick de
 *   un segundo lo provee la pantalla (un único `Instant` "ahora" para toda la grilla): crear un
 *   timer por tarjeta multiplicaría corrutinas y recomposiciones sin ninguna ganancia. Se recibe
 *   como `Long` y no como `Duration` porque `java.time.Duration` es inestable para el compilador
 *   de Compose y rompería la skippability de la tarjeta.
 *   Un valor negativo (la oferta ya venció mientras estaba en pantalla) se recorta a cero: nunca
 *   se muestran números negativos.
 *
 * Lleva la animación `pulse-slow` de DESIGN_SYSTEM.md como efecto de respiración.
 */
@Composable
fun CountdownTimer(
    remainingSeconds: Long,
    modifier: Modifier = Modifier,
) {
    val safeSeconds = remainingSeconds.coerceAtLeast(0L)
    val days = safeSeconds / SecondsPerDay
    val hours = (safeSeconds % SecondsPerDay) / SecondsPerHour
    val minutes = (safeSeconds % SecondsPerHour) / SecondsPerMinute
    val seconds = safeSeconds % SecondsPerMinute

    val pulseAlpha = rememberPulseAlpha()
    val spoken = spokenCountdown(days, hours, minutes, seconds)

    StatePill(
        state = ExpirationState.URGENT,
        // El alpha se lee dentro del bloque de `graphicsLayer` a propósito: así el pulso solo
        // reejecuta la fase de dibujo, sin recomponer la pastilla una vez por frame.
        modifier = modifier
            .graphicsLayer { alpha = pulseAlpha.value }
            .clearAndSetSemantics { contentDescription = spoken },
    ) {
        Text(
            text = "%dd %02d:%02d:%02d".format(days, hours, minutes, seconds),
            color = ExpirationState.URGENT.colors.text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                // Cifras tabulares: sin esto los dígitos cambian de ancho y el texto tiembla
                // cada segundo.
                fontFeatureSettings = "tnum",
            ),
            maxLines = 1,
        )
    }
}

/**
 * Alpha del `pulse-slow`: 1 → 0.6 → 1 en bucle, [Motion.PulseSlowMillis] por ciclo completo
 * (de ahí la mitad por tramo, con `RepeatMode.Reverse`).
 *
 * Si el usuario pidió reducir movimiento, `durationOrInstant` devuelve 0 y no se crea ninguna
 * transición infinita: se devuelve un alpha fijo. La rama es segura aunque haya un `remember`
 * de por medio porque `LocalReduceMotion` se provee una sola vez en `AppTheme` y no cambia
 * mientras la app está en pantalla.
 */
@Composable
private fun rememberPulseAlpha(): State<Float> {
    val cycleMillis = Motion.durationOrInstant(Motion.PulseSlowMillis, LocalReduceMotion.current)
    if (cycleMillis == 0) return remember { mutableFloatStateOf(1f) }

    val transition = rememberInfiniteTransition(label = "pulse-slow")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = Motion.PulseMinAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleMillis / 2, easing = Motion.EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-slow-alpha",
    )
}

/**
 * Lectura del countdown para TalkBack. El texto visible ("2d 04:07:33") es una abreviatura
 * pensada para el ojo; leído en voz alta no significa nada.
 */
private fun spokenCountdown(days: Long, hours: Long, minutes: Long, seconds: Long): String {
    if (days == 0L && hours == 0L && minutes == 0L && seconds == 0L) return "Oferta expirada"
    val parts = buildList {
        if (days > 0) add(plural(days, "día", "días"))
        if (hours > 0) add(plural(hours, "hora", "horas"))
        if (minutes > 0) add(plural(minutes, "minuto", "minutos"))
        if (seconds > 0) add(plural(seconds, "segundo", "segundos"))
    }
    return "Expira en ${parts.joinToString(", ")}"
}

private fun plural(value: Long, singular: String, plural: String): String =
    "$value ${if (value == 1L) singular else plural}"

@Preview(name = "CountdownTimer", widthDp = 360, showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun CountdownTimerPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(AppColors.Background)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            horizontalAlignment = Alignment.Start,
        ) {
            CountdownTimer(remainingSeconds = 6 * SecondsPerDay + 4 * SecondsPerHour + 7 * SecondsPerMinute + 33)
            CountdownTimer(remainingSeconds = 59)
            // Ya venció mientras estaba en pantalla: todo en cero, jamás negativos.
            CountdownTimer(remainingSeconds = 0)
            CountdownTimer(remainingSeconds = -9_999)
        }
    }
}
