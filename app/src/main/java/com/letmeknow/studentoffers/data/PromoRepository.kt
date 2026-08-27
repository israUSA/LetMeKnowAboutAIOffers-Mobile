package com.letmeknow.studentoffers.data

import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.feature.promos.ErrorKind
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de datos. Lo implementa `feat/data`; lo consumen el ViewModel y los workers de
 * notificaciones.
 *
 * `promos` es la fuente de verdad observable (respaldada por Room, para que la app abra con
 * datos aunque no haya red) y `refresh` es la acción explícita que va a la Edge Function.
 * Separarlos es lo que hace posible el caché offline y el pull-to-refresh sin estados raros.
 */
interface PromoRepository {

    /** Emite lo que haya en caché y vuelve a emitir con cada refresh exitoso. */
    val promos: Flow<List<Promo>>

    /**
     * Pide la lista a `promos-batch` y actualiza el caché.
     *
     * No lanza: los cuatro casos de fallo del contrato (red, HTTP, `success:false` y `data`
     * que no es array) vuelven como [ErrorKind] dentro de un `Result` fallido.
     */
    suspend fun refresh(): Result<Unit>

    /** Ids de las ofertas que el usuario marcó para recibir aviso. */
    val followedIds: Flow<Set<Long>>

    suspend fun setFollowed(promoId: Long, followed: Boolean)

    /** Ids que el usuario ya reclamó (tocó el CTA). Se usa para no recordarle de más. */
    val claimedIds: Flow<Set<Long>>

    suspend fun markClaimed(promoId: Long)
}
