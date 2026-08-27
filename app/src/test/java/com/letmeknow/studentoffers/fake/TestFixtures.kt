package com.letmeknow.studentoffers.fake

import com.letmeknow.studentoffers.core.time.Clock
import com.letmeknow.studentoffers.data.local.PrefsDataSource
import com.letmeknow.studentoffers.data.local.PromoLocalDataSource
import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

/**
 * Instante fijo de referencia para todos los tests. Las reglas de expiración se verifican
 * moviendo este reloj, nunca esperando tiempo real.
 */
val NOW: Instant = Instant.parse("2026-08-26T12:00:00Z")

/** Reloj mutable. `Clock` es una `fun interface`, pero los tests necesitan mover la hora. */
class FakeClock(var instant: Instant = NOW) : Clock {
    override fun now(): Instant = instant
}

fun promo(
    id: Long = 1L,
    company: String = "GitHub",
    title: String = "GitHub Copilot gratis",
    description: String = "Descripción larga que la tarjeta oculta hasta expandirse.",
    reclaimLink: String = "https://example.com/$id",
    createdAt: Instant = NOW.minusSeconds(30 * SECONDS_PER_DAY),
    expiresAt: Instant? = null,
): Promo = Promo(
    id = id,
    company = company,
    title = title,
    description = description,
    reclaimLink = reclaimLink,
    createdAt = createdAt,
    expiresAt = expiresAt,
)

const val SECONDS_PER_DAY: Long = 86_400L

fun days(count: Long): Long = count * SECONDS_PER_DAY

/**
 * Caché en memoria.
 *
 * Los tests del repositorio corren como unit tests de JVM, donde Room no existe; sustituir
 * la interfaz por esta implementación es lo que deja al repositorio testeable contra
 * MockWebServer sin emulador.
 */
class FakePromoLocalDataSource(initial: List<Promo> = emptyList()) : PromoLocalDataSource {

    private val state = MutableStateFlow(initial)

    override val promos: Flow<List<Promo>> = state

    val cached: List<Promo> get() = state.value

    override suspend fun replaceAll(promos: List<Promo>) {
        state.value = promos
    }
}

class FakePrefsDataSource(
    followed: Set<Long> = emptySet(),
    claimed: Set<Long> = emptySet(),
) : PrefsDataSource {

    private val followedState = MutableStateFlow(followed)
    private val claimedState = MutableStateFlow(claimed)

    override val followedIds: Flow<Set<Long>> = followedState

    override val claimedIds: Flow<Set<Long>> = claimedState

    override suspend fun setFollowed(promoId: Long, followed: Boolean) {
        followedState.value =
            if (followed) followedState.value + promoId else followedState.value - promoId
    }

    override suspend fun markClaimed(promoId: Long) {
        claimedState.value = claimedState.value + promoId
    }
}
