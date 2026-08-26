package com.letmeknow.aioffers.data.local

import com.letmeknow.aioffers.domain.model.Promo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Caché del catálogo, en términos de dominio.
 *
 * Es una interfaz y no el DAO pelado por dos razones: el repositorio no tiene que saber que
 * abajo hay Room, y los tests del repositorio corren como unit tests de JVM, donde Room no
 * existe. La implementación real es [RoomPromoLocalDataSource].
 */
interface PromoLocalDataSource {

    /** Emite lo cacheado y vuelve a emitir con cada [replaceAll]. */
    val promos: Flow<List<Promo>>

    /** Reemplaza el catálogo completo por el que acaba de devolver la red. */
    suspend fun replaceAll(promos: List<Promo>)
}

class RoomPromoLocalDataSource(private val dao: PromoDao) : PromoLocalDataSource {

    override val promos: Flow<List<Promo>> =
        dao.observeAll().map { entities -> entities.map(PromoEntity::toDomain) }

    override suspend fun replaceAll(promos: List<Promo>) {
        dao.replaceAll(promos.map(Promo::toEntity))
    }
}
