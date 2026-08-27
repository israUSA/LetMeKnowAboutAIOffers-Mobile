package com.letmeknow.studentoffers.domain

import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.feature.promos.PromoTab
import com.letmeknow.studentoffers.feature.promos.TabCounts

/**
 * Búsqueda y filtro por tab, exactamente como los define DATA_AND_API.md.
 *
 * `PromoTab` y `TabCounts` viven en `feature/promos` porque son parte del contrato de estado
 * de la pantalla; se importan en vez de duplicarse para que no existan dos enums de tab que
 * puedan divergir.
 */
object PromoFilter {

    /**
     * Case-insensitive sobre **`company` y `title` únicamente**.
     *
     * `description` queda deliberadamente fuera: es el texto largo que la tarjeta oculta
     * hasta expandirse, y buscar dentro de él haría aparecer resultados cuyo motivo de match
     * el usuario no puede ver. La web se comporta igual.
     */
    fun search(promos: List<Promo>, query: String): List<Promo> {
        val term = query.trim()
        if (term.isEmpty()) return promos

        return promos.filter { promo ->
            promo.company.contains(term, ignoreCase = true) ||
                promo.title.contains(term, ignoreCase = true)
        }
    }

    fun byTab(promos: List<Promo>, tab: PromoTab): List<Promo> = when (tab) {
        PromoTab.ALL -> promos
        PromoTab.PERMANENT -> promos.filter { it.expiresAt == null }
        PromoTab.LIMITED -> promos.filter { it.expiresAt != null }
    }

    /**
     * Contadores de los tabs.
     *
     * Se calculan **siempre sobre la lista completa sin filtrar**: el parámetro se llama
     * `all` para que llamarlo con el resultado de [search] o de [byTab] se lea como el error
     * que es. Un contador que se mueve con la búsqueda deja de decirle al usuario cuántas
     * ofertas hay en el otro tab, que es todo lo que el contador sirve.
     */
    fun counts(all: List<Promo>): TabCounts {
        val permanent = all.count { it.expiresAt == null }
        return TabCounts(all = all.size, permanent = permanent, limited = all.size - permanent)
    }

    /** Búsqueda y tab en el orden en que los aplica la pantalla. */
    fun apply(all: List<Promo>, query: String, tab: PromoTab): List<Promo> =
        byTab(search(all, query), tab)
}
