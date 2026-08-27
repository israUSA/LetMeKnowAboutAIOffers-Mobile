package com.letmeknow.studentoffers.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PromoDao {

    /** Emite el caché completo y vuelve a emitir con cada escritura. */
    @Query("SELECT * FROM promos")
    fun observeAll(): Flow<List<PromoEntity>>

    @Query("SELECT * FROM promos")
    suspend fun getAll(): List<PromoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(promos: List<PromoEntity>)

    @Query("DELETE FROM promos")
    suspend fun deleteAll()

    /**
     * Reemplaza el catálogo entero en una sola transacción.
     *
     * Es un reemplazo y no un upsert incremental porque `promos-batch` devuelve siempre el
     * catálogo completo (no hay paginación, ver PRODUCT_OVERVIEW.md): una oferta que
     * desapareció del backend tiene que desaparecer del caché. La transacción evita que un
     * colector del `Flow` vea la tabla vacía a mitad de camino.
     */
    @Transaction
    suspend fun replaceAll(promos: List<PromoEntity>) {
        deleteAll()
        upsertAll(promos)
    }
}
