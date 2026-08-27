package com.letmeknow.studentoffers.fake

import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.notifications.Notifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Repositorio en memoria completo, para lo que se prueba fuera del ViewModel: la
 * reverificación del recordatorio y la detección de ofertas nuevas.
 *
 * `refresh` no va a la red: reemplaza el catálogo por [refreshedPromos], que es exactamente lo
 * que hace `DefaultPromoRepository` cuando la llamada sale bien. Eso alcanza para verificar la
 * secuencia "leer ids → refrescar → comparar" sin MockWebServer ni Room.
 */
class FakePromoRepository(
    promos: List<Promo> = emptyList(),
    followed: Set<Long> = emptySet(),
    claimed: Set<Long> = emptySet(),
    private val refreshedPromos: List<Promo>? = null,
    private val refreshResult: Result<Unit> = Result.success(Unit),
) : PromoRepository {

    private val promosState = MutableStateFlow(promos)
    private val followedState = MutableStateFlow(followed)
    private val claimedState = MutableStateFlow(claimed)

    override val promos: Flow<List<Promo>> = promosState
    override val followedIds: Flow<Set<Long>> = followedState
    override val claimedIds: Flow<Set<Long>> = claimedState

    override suspend fun refresh(): Result<Unit> {
        if (refreshResult.isSuccess && refreshedPromos != null) {
            promosState.value = refreshedPromos
        }
        return refreshResult
    }

    override suspend fun setFollowed(promoId: Long, followed: Boolean) {
        followedState.value =
            if (followed) followedState.value + promoId else followedState.value - promoId
    }

    override suspend fun markClaimed(promoId: Long) {
        claimedState.value = claimedState.value + promoId
    }
}

/**
 * [Notifier] que solo anota qué le pidieron.
 *
 * Es lo que hace testeable "al dejar de seguir una oferta se cancela su work" sin WorkManager:
 * lo que hay que verificar es que el ViewModel pida la cancelación, no cómo la traduce
 * `DefaultNotifier` a un work único.
 */
class RecordingNotifier : Notifier {

    val scheduled = mutableListOf<Long>()
    val cancelled = mutableListOf<Long>()
    val notified = mutableListOf<List<Promo>>()

    override suspend fun scheduleClaimReminder(promo: Promo) {
        scheduled += promo.id
    }

    override suspend fun cancelClaimReminder(promoId: Long) {
        cancelled += promoId
    }

    override suspend fun rescheduleAll() = Unit

    override suspend fun notifyNewPromos(promos: List<Promo>) {
        notified += promos
    }
}
