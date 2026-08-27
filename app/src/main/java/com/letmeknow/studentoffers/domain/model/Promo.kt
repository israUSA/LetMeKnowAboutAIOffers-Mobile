package com.letmeknow.studentoffers.domain.model

import java.time.Instant

/**
 * Modelo de dominio de una oferta.
 *
 * Diferencias deliberadas con el DTO de la API (ver DATA_AND_API.md):
 * - Las fechas son `Instant`, ya parseadas. Ningún consumidor debe volver a parsear strings.
 * - `expiresAt == null` significa oferta permanente, sin vencimiento.
 * - `startDate` está declarado en el contrato de la API pero no se usa en ninguna pantalla;
 *   se omite acá a propósito para no arrastrar un campo muerto al dominio.
 */
data class Promo(
    val id: Long,
    val company: String,
    val title: String,
    val description: String,
    val reclaimLink: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
)
