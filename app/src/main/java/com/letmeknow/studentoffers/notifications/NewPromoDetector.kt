package com.letmeknow.studentoffers.notifications

import com.letmeknow.studentoffers.domain.model.Promo

/**
 * Compara el catálogo recién traído contra los ids que ya había en Room.
 *
 * Es la aproximación sin backend a "apareció una oferta nueva": no hay push, así que lo único
 * que se puede hacer es refrescar cada tanto y mirar qué ids no estaban antes.
 *
 * Kotlin puro y sin reloj: la novedad de una oferta no depende de la hora, solo de si su id ya
 * estaba en el caché.
 */
object NewPromoDetector {

    /**
     * @param knownIds ids que estaban en el caché **antes** del refresh.
     * @param refreshed catálogo completo devuelto por el refresh.
     *
     * Con el caché vacío devuelve una lista vacía a propósito: en la primera corrida no hay
     * contra qué comparar, y avisar de "20 ofertas nuevas" en la primera instalación sería
     * ruido, no una novedad.
     */
    fun newPromos(knownIds: Set<Long>, refreshed: List<Promo>): List<Promo> {
        if (knownIds.isEmpty()) return emptyList()
        return refreshed.filter { it.id !in knownIds }
    }
}
