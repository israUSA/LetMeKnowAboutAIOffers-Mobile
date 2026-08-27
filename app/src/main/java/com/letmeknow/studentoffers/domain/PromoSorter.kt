package com.letmeknow.studentoffers.domain

import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo

/**
 * Orden de la grilla: urgent -> warning -> comfortable -> permanent.
 *
 * `ExpirationState` declara sus constantes en ese orden a propósito, así que alcanza con
 * ordenar por `ordinal`; un comparador con números mágicos sería una segunda fuente de
 * verdad para lo mismo.
 *
 * Se recalcula sobre la lista ya buscada y filtrada, no sobre el catálogo completo: el
 * estado depende de la hora actual y el orden tiene que reflejar lo que el usuario ve.
 */
object PromoSorter {

    /** `sortedBy` es estable, así que dentro de un mismo estado se respeta el orden de entrada. */
    fun sortByUrgency(promos: List<Promo>, rules: ExpirationRules): List<Promo> =
        promos.sortedBy { rules.getExpirationState(it).ordinal }

    /** Variante para cuando el estado ya fue calculado y no conviene recalcularlo por elemento. */
    fun <T> sortByUrgency(items: List<T>, state: (T) -> ExpirationState): List<T> =
        items.sortedBy { state(it).ordinal }
}
