package com.letmeknow.studentoffers.data

import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.flow.Flow

interface PromoRepository {

    val promos: Flow<List<Promo>>

    suspend fun refresh(): Result<Unit>

    val claimedIds: Flow<Set<Long>>

    suspend fun markClaimed(promoId: Long)
}
