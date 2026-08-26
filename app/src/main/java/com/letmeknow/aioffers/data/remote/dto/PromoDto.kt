package com.letmeknow.aioffers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Una oferta tal como la devuelve `promos-batch`. Refleja el contrato de DATA_AND_API.md
 * campo por campo, incluido `start_date`, que la API declara pero ninguna pantalla usa: se
 * acepta acá y se descarta al mapear al dominio.
 *
 * `expires_at` nulo significa oferta permanente.
 */
@Serializable
data class PromoDto(
    val id: Long,
    val company: String,
    val title: String,
    val description: String = "",
    @SerialName("reclaim_link") val reclaimLink: String = "",
    @SerialName("created_at") val createdAt: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)

/**
 * Sobre de la respuesta: `{ "success": true, "data": [...], "count": 8 }`.
 *
 * `data` se deja como [JsonElement] a propósito. El contrato de error exige distinguir
 * "`data` no es un array" de "falló la red", y eso solo se puede decidir mirando el JSON ya
 * parseado; si se declarara `List<PromoDto>` el fallo llegaría como una excepción del
 * conversor, indistinguible de un cuerpo corrupto.
 */
@Serializable
data class PromosBatchResponse(
    val success: Boolean = false,
    val data: JsonElement? = null,
    val count: Int? = null,
)
