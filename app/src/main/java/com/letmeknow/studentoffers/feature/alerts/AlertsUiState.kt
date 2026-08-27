package com.letmeknow.studentoffers.feature.alerts

import androidx.compose.runtime.Immutable
import com.letmeknow.studentoffers.domain.model.ExpirationState

/**
 * Una oferta seguida, ya resuelta para pintar en el bottom sheet.
 *
 * Es un modelo propio y no `PromoUiModel` porque el sheet muestra menos y algo distinto: no
 * necesita descripción, ni link, ni porcentaje de progreso, y sí necesita una etiqueta de
 * vencimiento **siempre presente** — en la grilla el estado urgente la deja vacía porque la
 * reemplaza el countdown en vivo, y acá no hay countdown.
 */
@Immutable
data class AlertUiModel(
    val id: Long,
    val company: String,
    val title: String,
    val state: ExpirationState,
    val expirationLabel: String,
    val isClaimed: Boolean,
)

/**
 * Estado del destino de avisos.
 *
 * `isOpen` vive acá, dentro del estado de la pantalla, y no en un `remember` del sheet: es el
 * ViewModel quien decide si el destino está abierto, igual que con `expandedId`. Eso es lo que
 * hace que migrar a Navigation Compose sea un cambio local — el día que exista un back stack,
 * `isOpen` se reemplaza por una entrada de ruta y nada más de la pantalla cambia.
 */
@Immutable
data class AlertsUiState(
    val isOpen: Boolean = false,
    val alerts: List<AlertUiModel> = emptyList(),
)
