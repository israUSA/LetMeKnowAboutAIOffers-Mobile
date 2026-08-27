package com.letmeknow.studentoffers.feature.promos

import androidx.compose.runtime.Immutable
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.feature.alerts.AlertsUiState
import com.letmeknow.studentoffers.domain.model.Promo

/** Tab de filtro. Los contadores se calculan SIEMPRE sobre la lista completa sin filtrar. */
enum class PromoTab { ALL, PERMANENT, LIMITED }

/**
 * Contrato de error de la app. Cualquiera de estos casos muestra la pantalla de error;
 * ninguno debe crashear.
 */
sealed interface ErrorKind {
    /** Falta `SUPABASE_URL` y/o `SUPABASE_ANON_KEY` en `local.properties`. */
    data class MissingConfig(val missingKeys: List<String>) : ErrorKind

    /** Sin conexión, timeout, DNS. */
    data object Network : ErrorKind

    /** La llamada respondió 4xx/5xx. */
    data class Http(val code: Int) : ErrorKind

    /** Respondió 200 pero `success` vino en `false`, o `data` no era un array. */
    data object MalformedPayload : ErrorKind
}

/** Una oferta ya resuelta para pintar: no hay que recalcular nada en el composable. */
@Immutable
data class PromoUiModel(
    val promo: Promo,
    val state: ExpirationState,
    /** Porcentaje de tiempo restante, 0..100. `null` si la oferta es permanente. */
    val timeRemainingPercent: Float?,
    /** "Siempre disponible", "Expira mañana", "Expira el 15 mar 2027"… Vacío si es urgente. */
    val expirationLabel: String,
    val isFollowed: Boolean,
    val isClaimed: Boolean,
)

@Immutable
data class TabCounts(val all: Int, val permanent: Int, val limited: Int)

/**
 * Estado único de la pantalla principal.
 *
 * `expandedId` vive acá y no en cada tarjeta: DESIGN_SYSTEM.md exige que solo una tarjeta
 * pueda estar expandida a la vez en toda la grilla, y eso solo se garantiza si el estado
 * está izado a un único dueño.
 */
sealed interface PromosUiState {

    /** Skeletons con shimmer. */
    data object Loading : PromosUiState

    data class Error(val kind: ErrorKind) : PromosUiState

    @Immutable
    data class Content(
        /** Ya buscadas, filtradas y ordenadas por urgencia. Puede venir vacía. */
        val promos: List<PromoUiModel>,
        val query: String,
        val tab: PromoTab,
        val counts: TabCounts,
        val expandedId: Long?,
        val isRefreshing: Boolean,
        /** Se muestran datos del caché porque el último refresh falló. */
        val isStale: Boolean,
        /**
         * El destino de avisos: si está abierto y qué ofertas seguidas muestra.
         *
         * Se calcula sobre el catálogo **completo**, no sobre [promos]: lo que el usuario
         * sigue no puede depender de la búsqueda ni del tab activo.
         */
        val alerts: AlertsUiState = AlertsUiState(),
    ) : PromosUiState {
        /**
         * Distinto del estado de error: hay datos, pero la búsqueda o el tab activo no
         * devuelven nada.
         */
        val isEmpty: Boolean get() = promos.isEmpty()
    }
}
