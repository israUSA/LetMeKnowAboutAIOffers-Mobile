package com.letmeknow.studentoffers.data

import com.letmeknow.studentoffers.data.local.PrefsDataSource
import com.letmeknow.studentoffers.data.local.PromoLocalDataSource
import com.letmeknow.studentoffers.data.remote.PromosRemoteDataSource
import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Implementación única de [PromoRepository].
 *
 * La división que importa: `promos` sale **siempre** del caché local y `refresh` es la única
 * cosa que toca la red. Por eso la app abre con datos aunque no haya conexión, y por eso el
 * pull-to-refresh puede fallar sin vaciar la pantalla — el `Flow` sigue emitiendo lo último
 * bueno mientras el ViewModel marca el estado como stale.
 *
 * No expone ningún `CoroutineScope`: todo es `suspend` y el dueño del scope es quien llama
 * (el ViewModel o un worker de notificaciones).
 */
class DefaultPromoRepository(
    private val remote: PromosRemoteDataSource,
    private val local: PromoLocalDataSource,
    private val prefs: PrefsDataSource,
) : PromoRepository {

    override val promos: Flow<List<Promo>> = local.promos

    override val followedIds: Flow<Set<Long>> = prefs.followedIds

    override val claimedIds: Flow<Set<Long>> = prefs.claimedIds

    /**
     * Nunca lanza, salvo cancelación. El caché solo se pisa si la red respondió bien: un
     * fallo deja intacto lo que ya había, que es la mitad del contrato offline.
     */
    override suspend fun refresh(): Result<Unit> {
        val fetched = remote.fetchPromos().getOrElse { return Result.failure(it) }
        return try {
            local.replaceAll(fetched)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Escribir el caché falló (disco lleno, base corrupta). Se reporta como fallo del
            // refresh en vez de propagarse: la pantalla tiene que mostrar error, no crashear.
            Result.failure(e)
        }
    }

    override suspend fun setFollowed(promoId: Long, followed: Boolean) =
        prefs.setFollowed(promoId, followed)

    override suspend fun markClaimed(promoId: Long) = prefs.markClaimed(promoId)
}
