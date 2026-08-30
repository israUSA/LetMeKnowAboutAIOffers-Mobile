package com.letmeknow.studentoffers.data

import com.letmeknow.studentoffers.data.local.PrefsDataSource
import com.letmeknow.studentoffers.data.local.PromoLocalDataSource
import com.letmeknow.studentoffers.data.remote.PromosRemoteDataSource
import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class DefaultPromoRepository(
    private val remote: PromosRemoteDataSource,
    private val local: PromoLocalDataSource,
    private val prefs: PrefsDataSource,
) : PromoRepository {

    override val promos: Flow<List<Promo>> = local.promos

    override val claimedIds: Flow<Set<Long>> = prefs.claimedIds

    override suspend fun refresh(): Result<Unit> {
        val fetched = remote.fetchPromos().getOrElse { return Result.failure(it) }
        return try {
            local.replaceAll(fetched)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markClaimed(promoId: Long) = prefs.markClaimed(promoId)
}
