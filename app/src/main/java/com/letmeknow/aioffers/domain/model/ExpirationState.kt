package com.letmeknow.aioffers.domain.model

/**
 * Estado de vencimiento de una oferta. Los umbrales exactos están en DATA_AND_API.md
 * y se calculan en `ExpirationRules`.
 *
 * El orden de declaración define el orden de la grilla: urgent → warning → comfortable →
 * permanent. Ordenar por `ordinal` es suficiente; no hace falta un comparador aparte con
 * números mágicos.
 */
enum class ExpirationState {
    /** `expires_at` a 7 días o menos. Único estado que muestra countdown en vivo. */
    URGENT,

    /** `expires_at` a más de 7 y hasta 30 días. */
    WARNING,

    /** `expires_at` a más de 30 días. */
    COMFORTABLE,

    /** `expires_at == null`: la oferta no vence. */
    PERMANENT,
}
