package com.letmeknow.aioffers.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Caché local del catálogo. No es una base de datos de usuario: se puede borrar entera sin
 * perder nada que el backend no pueda devolver, por eso las migraciones son destructivas
 * (ver `AppContainer`).
 */
@Database(entities = [PromoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promoDao(): PromoDao

    companion object {
        const val NAME = "promos.db"
    }
}
