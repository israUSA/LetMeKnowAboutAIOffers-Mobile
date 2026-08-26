package com.letmeknow.aioffers.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.letmeknow.aioffers.domain.model.Promo
import java.time.Instant

/**
 * Fila del caché del catálogo.
 *
 * Las fechas se guardan como epoch millis y no como texto ISO: el dominio ya trabaja con
 * `Instant`, y guardar el string obligaría a reparsear en cada lectura de Room, que es
 * justamente lo que `Promo` documenta que nadie debe volver a hacer.
 *
 * `expiresAt` nulo significa oferta permanente, igual que en la API y en el dominio.
 */
@Entity(tableName = "promos")
data class PromoEntity(
    @PrimaryKey val id: Long,
    val company: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "reclaim_link") val reclaimLink: String,
    @ColumnInfo(name = "created_at") val createdAtEpochMillis: Long,
    @ColumnInfo(name = "expires_at") val expiresAtEpochMillis: Long?,
)

internal fun PromoEntity.toDomain(): Promo = Promo(
    id = id,
    company = company,
    title = title,
    description = description,
    reclaimLink = reclaimLink,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    expiresAt = expiresAtEpochMillis?.let(Instant::ofEpochMilli),
)

internal fun Promo.toEntity(): PromoEntity = PromoEntity(
    id = id,
    company = company,
    title = title,
    description = description,
    reclaimLink = reclaimLink,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    expiresAtEpochMillis = expiresAt?.toEpochMilli(),
)
