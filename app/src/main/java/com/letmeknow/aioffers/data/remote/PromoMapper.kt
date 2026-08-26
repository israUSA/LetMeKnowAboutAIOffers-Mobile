package com.letmeknow.aioffers.data.remote

import com.letmeknow.aioffers.data.remote.dto.PromoDto
import com.letmeknow.aioffers.domain.model.Promo
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Formato de las fechas ISO que devuelve Supabase.
 *
 * Postgres puede mandar `2027-03-15T00:00:00Z`, `2027-03-15T00:00:00+00:00` o, cuando la
 * columna es `timestamp without time zone`, `2027-03-15T00:00:00` sin offset. `Instant.parse`
 * solo acepta la primera forma en el `java.time` de API 26, así que el offset se declara
 * opcional y se asume UTC cuando falta, que es como lo interpreta Supabase.
 */
private val ISO_INSTANT: DateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .optionalStart()
    .appendOffsetId()
    .optionalEnd()
    .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
    .toFormatter()

/**
 * Parsea una fecha ISO del payload.
 *
 * Lanza `DateTimeParseException` si el texto no es una fecha. El único llamador es
 * [PromosRemoteDataSource], que lo trata como payload corrupto (`ErrorKind.MalformedPayload`);
 * no se devuelve `null` porque una fecha ilegible no es un valor válido de dominio.
 */
internal fun parseIsoInstant(raw: String): Instant = ISO_INSTANT.parse(raw.trim(), Instant::from)

/**
 * DTO -> dominio. Las fechas quedan parseadas acá y en ningún otro lado: `Promo` expone
 * `Instant`, y `start_date` se descarta por no usarse en ninguna pantalla (DATA_AND_API.md).
 */
internal fun PromoDto.toDomain(): Promo = Promo(
    id = id,
    company = company,
    title = title,
    description = description,
    reclaimLink = reclaimLink,
    createdAt = parseIsoInstant(createdAt),
    expiresAt = expiresAt?.takeIf { it.isNotBlank() }?.let(::parseIsoInstant),
)
