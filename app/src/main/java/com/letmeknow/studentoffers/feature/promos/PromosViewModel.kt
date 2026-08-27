package com.letmeknow.studentoffers.feature.promos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letmeknow.studentoffers.core.config.AppConfig
import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.data.toErrorKind
import com.letmeknow.studentoffers.domain.ExpirationRules
import com.letmeknow.studentoffers.domain.PromoFilter
import com.letmeknow.studentoffers.domain.PromoSorter
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.feature.alerts.AlertUiModel
import com.letmeknow.studentoffers.feature.alerts.AlertsUiState
import com.letmeknow.studentoffers.notifications.Notifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Dueño único del estado de la pantalla principal.
 *
 * Expone un solo `StateFlow<PromosUiState>` y es dueño de `query`, `tab`, `expandedId`,
 * `isRefreshing` e `isStale`. Los composables son stateless y reciben `(state, onEvent)`;
 * `expandedId` vive acá y no en la tarjeta porque DESIGN_SYSTEM.md exige que solo una esté
 * expandida a la vez en toda la grilla, y eso solo se garantiza con un único dueño.
 *
 * [config] entra por constructor y [repositoryProvider] es perezoso a propósito: si la
 * configuración es [AppConfig.Missing] el proveedor **nunca se invoca**, así que no se
 * construye cliente HTTP, no se abre base de datos y no se toca la red. Es la mitad de
 * runtime de la corrección documentada en `AppConfig`; la otra mitad son los `by lazy` de
 * `AppContainer`.
 */
class PromosViewModel(
    private val config: AppConfig,
    private val rules: ExpirationRules,
    repositoryProvider: () -> PromoRepository,
    notifierProvider: () -> Notifier,
) : ViewModel() {

    /** Entradas que el ViewModel controla. Van juntas para que el estado se arme de un combine. */
    private data class Inputs(
        val query: String = "",
        val tab: PromoTab = PromoTab.ALL,
        val expandedId: Long? = null,
        val isRefreshing: Boolean = false,
        val isStale: Boolean = false,
        val error: ErrorKind? = null,
        val isInitialLoad: Boolean = true,
        val isAlertsOpen: Boolean = false,
    )

    private val inputs = MutableStateFlow(Inputs())
    private var refreshJob: Job? = null

    private val repository: PromoRepository? =
        if (config is AppConfig.Valid) repositoryProvider() else null

    /**
     * Perezoso por la misma razón que [repository]: con [AppConfig.Missing] no hay pantalla
     * de datos y no hay nada que programar, así que tampoco hay que construir nada.
     */
    private val notifier: Notifier? =
        if (config is AppConfig.Valid) notifierProvider() else null

    /**
     * Ticker de countdown: **uno solo para toda la pantalla**, no uno por tarjeta.
     *
     * Se enciende solo si hay al menos una oferta [ExpirationState.URGENT] (es el único
     * estado que muestra countdown) y se apaga cuando no queda ninguna, así que una pantalla
     * sin urgencias no despierta a nadie una vez por segundo. `flatMapLatest` cancela el
     * ticker viejo en cuanto esa condición cambia.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val ticker: Flow<Long> = (repository?.promos ?: flowOf(emptyList()))
        .map { promos -> promos.any { rules.getExpirationState(it) == ExpirationState.URGENT } }
        .distinctUntilChanged()
        .flatMapLatest { hasUrgent ->
            if (!hasUrgent) {
                flowOf(0L)
            } else {
                flow {
                    var tick = 0L
                    while (true) {
                        emit(tick++)
                        delay(TICK_INTERVAL_MILLIS)
                    }
                }
            }
        }

    val state: StateFlow<PromosUiState> = when (config) {
        is AppConfig.Missing ->
            MutableStateFlow(PromosUiState.Error(ErrorKind.MissingConfig(config.missingKeys)))

        is AppConfig.Valid -> {
            val repo = requireNotNull(repository)
            combine(
                repo.promos,
                repo.followedIds,
                repo.claimedIds,
                inputs,
                ticker,
            ) { promos, followed, claimed, current, _ ->
                buildState(promos, followed, claimed, current)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
                initialValue = PromosUiState.Loading,
            )
        }
    }

    init {
        // La carga inicial arranca acá y no en el primer colector: la pantalla tiene que
        // pedir datos aunque el usuario todavía no haya hecho scroll ni pull-to-refresh.
        // Con config Missing no hace nada, que es justamente el punto.
        refresh(initial = true)
    }

    fun onQueryChange(query: String) = inputs.update { it.copy(query = query) }

    fun onTabChange(tab: PromoTab) = inputs.update { it.copy(tab = tab) }

    /** Expandir una tarjeta colapsa la anterior; volver a tocar la misma la colapsa. */
    fun onCardClick(promoId: Long) = inputs.update {
        it.copy(expandedId = if (it.expandedId == promoId) null else promoId)
    }

    fun onRefresh() = refresh(initial = false)

    /** Mismo camino que el pull-to-refresh: el botón de la pantalla de error reintenta. */
    fun onRetry() = refresh(initial = true)

    fun onAlertsOpen() = inputs.update { it.copy(isAlertsOpen = true) }

    fun onAlertsDismiss() = inputs.update { it.copy(isAlertsOpen = false) }

    /**
     * Persistir el seguimiento y programar el aviso son un solo gesto del usuario, así que
     * van en la misma corrutina y en este orden: primero se guarda la preferencia, que es lo
     * que el usuario espera que quede aunque el permiso de notificaciones falte.
     *
     * Al dejar de seguir se cancela el work pendiente en el acto. Un recordatorio de una
     * oferta que ya no se sigue no solo es ruido: es work huérfano que sobrevive reinicios.
     */
    fun onFollowToggle(promoId: Long, followed: Boolean) {
        val repo = repository ?: return
        val notifier = notifier ?: return

        viewModelScope.launch {
            repo.setFollowed(promoId, followed)

            if (!followed) {
                notifier.cancelClaimReminder(promoId)
                return@launch
            }

            // Seguir algo ya reclamado no programa nada: el recordatorio existe para avisar
            // de lo que falta reclamar.
            if (promoId in repo.claimedIds.first()) return@launch
            repo.promos.first().firstOrNull { it.id == promoId }
                ?.let { notifier.scheduleClaimReminder(it) }
        }
    }

    /** Reclamar apaga el recordatorio: ya no hay nada de qué avisar. */
    fun onClaim(promoId: Long) {
        val repo = repository ?: return
        val notifier = notifier ?: return

        viewModelScope.launch {
            repo.markClaimed(promoId)
            notifier.cancelClaimReminder(promoId)
        }
    }

    private fun refresh(initial: Boolean) {
        val repo = repository ?: return
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            inputs.update { it.copy(isRefreshing = !initial, isInitialLoad = initial && it.isInitialLoad) }

            val kind = repo.refresh().exceptionOrNull()?.toErrorKind()

            inputs.update {
                it.copy(
                    isRefreshing = false,
                    isInitialLoad = false,
                    error = kind,
                    isStale = kind != null,
                )
            }
        }
    }

    private fun buildState(
        promos: List<Promo>,
        followed: Set<Long>,
        claimed: Set<Long>,
        current: Inputs,
    ): PromosUiState {
        val error = current.error
        return when {
            // El error solo se apodera de la pantalla si no hay nada que mostrar. Con caché
            // se prefiere mostrar datos viejos marcados como stale; vaciar la grilla por un
            // refresh fallido sería peor que la información desactualizada.
            error != null && promos.isEmpty() -> PromosUiState.Error(error)
            current.isInitialLoad && promos.isEmpty() -> PromosUiState.Loading
            else -> content(promos, followed, claimed, current)
        }
    }

    private fun content(
        promos: List<Promo>,
        followed: Set<Long>,
        claimed: Set<Long>,
        current: Inputs,
    ): PromosUiState.Content {
        val visible = PromoSorter.sortByUrgency(
            PromoFilter.apply(promos, current.query, current.tab),
            rules,
        )

        return PromosUiState.Content(
            promos = visible.map { promo ->
                PromoUiModel(
                    promo = promo,
                    state = rules.getExpirationState(promo),
                    timeRemainingPercent = rules.getTimeRemainingPercent(promo),
                    expirationLabel = rules.expirationLabel(promo),
                    isFollowed = promo.id in followed,
                    isClaimed = promo.id in claimed,
                )
            },
            query = current.query,
            tab = current.tab,
            // Sobre el catálogo completo, nunca sobre `visible`: ver PromoFilter.counts.
            counts = PromoFilter.counts(promos),
            expandedId = current.expandedId,
            isRefreshing = current.isRefreshing,
            isStale = current.isStale,
            alerts = AlertsUiState(
                isOpen = current.isAlertsOpen,
                alerts = alerts(promos, followed, claimed),
            ),
        )
    }

    /**
     * Las ofertas seguidas, sobre el catálogo completo y ordenadas por urgencia igual que la
     * grilla: la búsqueda y el tab filtran lo que se explora, no lo que el usuario ya marcó.
     *
     * La etiqueta se calcula con `formatRelativeDate` y no con `expirationLabel` a propósito:
     * esta última viene vacía en estado urgente porque en la tarjeta la reemplaza el countdown
     * en vivo, y en el sheet no hay countdown que la reemplace.
     */
    private fun alerts(
        promos: List<Promo>,
        followed: Set<Long>,
        claimed: Set<Long>,
    ): List<AlertUiModel> =
        PromoSorter.sortByUrgency(promos.filter { it.id in followed }, rules)
            .map { promo ->
                AlertUiModel(
                    id = promo.id,
                    company = promo.company,
                    title = promo.title,
                    state = rules.getExpirationState(promo),
                    expirationLabel = rules.formatRelativeDate(promo),
                    isClaimed = promo.id in claimed,
                )
            }

    private companion object {
        const val TICK_INTERVAL_MILLIS = 1_000L
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
    }
}
