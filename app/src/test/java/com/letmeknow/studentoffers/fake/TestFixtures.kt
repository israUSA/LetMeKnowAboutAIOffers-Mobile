package com.letmeknow.studentoffers.fake

import com.letmeknow.studentoffers.core.time.Clock
import com.letmeknow.studentoffers.data.local.PrefsDataSource
import com.letmeknow.studentoffers.data.local.PromoLocalDataSource
import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

val NOW: Instant = Instant.parse("2026-08-26T12:00:00Z")

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

class FakePromoLocalDataSource(initial: List<Promo> = emptyList()) : PromoLocalDataSource {

    private val state = MutableStateFlow(initial)

    override val promos: Flow<List<Promo>> = state

    val cached: List<Promo> get() = state.value

    override suspend fun replaceAll(promos: List<Promo>) {
        state.value = promos
    }
}

class FakePrefsDataSource(
    claimed: Set<Long> = emptySet(),
) : PrefsDataSource {

    private val claimedState = MutableStateFlow(claimed)

    override val claimedIds: Flow<Set<Long>> = claimedState

    override suspend fun markClaimed(promoId: Long) {
        claimedState.value = claimedState.value + promoId
    }
}
