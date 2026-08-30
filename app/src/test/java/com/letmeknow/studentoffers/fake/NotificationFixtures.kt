package com.letmeknow.studentoffers.fake

import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.data.local.NotificationsPreferences
import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.notifications.Notifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePromoRepository(
    promos: List<Promo> = emptyList(),
    claimed: Set<Long> = emptySet(),
    private val refreshedPromos: List<Promo>? = null,
    private val refreshResult: Result<Unit> = Result.success(Unit),
) : PromoRepository {

    private val promosState = MutableStateFlow(promos)
    private val claimedState = MutableStateFlow(claimed)

    override val promos: Flow<List<Promo>> = promosState
    override val claimedIds: Flow<Set<Long>> = claimedState

    override suspend fun refresh(): Result<Unit> {
        if (refreshResult.isSuccess && refreshedPromos != null) {
            promosState.value = refreshedPromos
        }
        return refreshResult
    }

    override suspend fun markClaimed(promoId: Long) {
        claimedState.value = claimedState.value + promoId
    }
}

class RecordingNotifier : Notifier {

    val notified = mutableListOf<List<Promo>>()

    override suspend fun notifyNewPromos(promos: List<Promo>) {
        notified += promos
    }
}

class FakeNotificationsPreferences(initial: Boolean = false) : NotificationsPreferences {

    private val state = MutableStateFlow(initial)

    override val enabled: Flow<Boolean> = state

    override suspend fun setEnabled(enabled: Boolean) {
        state.value = enabled
    }
}
