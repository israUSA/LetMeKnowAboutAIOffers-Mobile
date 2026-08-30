package com.letmeknow.studentoffers.feature.promos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letmeknow.studentoffers.core.config.AppConfig
import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.data.local.NotificationsPreferences
import com.letmeknow.studentoffers.data.toErrorKind
import com.letmeknow.studentoffers.domain.ExpirationRules
import com.letmeknow.studentoffers.domain.PromoFilter
import com.letmeknow.studentoffers.domain.PromoSorter
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PromosViewModel(
    private val config: AppConfig,
    private val rules: ExpirationRules,
    repositoryProvider: () -> PromoRepository,
    private val notificationsPreferences: NotificationsPreferences,
) : ViewModel() {

    private data class Inputs(
        val query: String = "",
        val tab: PromoTab = PromoTab.ALL,
        val expandedId: Long? = null,
        val isRefreshing: Boolean = false,
        val isStale: Boolean = false,
        val error: ErrorKind? = null,
        val isInitialLoad: Boolean = true,
    )

    private val inputs = MutableStateFlow(Inputs())
    private var refreshJob: Job? = null

    private val repository: PromoRepository? =
        if (config is AppConfig.Valid) repositoryProvider() else null

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
                repo.claimedIds,
                inputs,
                ticker,
            ) { promos, claimed, current, _ ->
                buildState(promos, claimed, current)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
                initialValue = PromosUiState.Loading,
            )
        }
    }

    val notificationsEnabled: StateFlow<Boolean> = notificationsPreferences.enabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = false,
        )

    init {
        refresh(initial = true)
    }

    fun onQueryChange(query: String) = inputs.update { it.copy(query = query) }

    fun onTabChange(tab: PromoTab) = inputs.update { it.copy(tab = tab) }

    fun onCardClick(promoId: Long) = inputs.update {
        it.copy(expandedId = if (it.expandedId == promoId) null else promoId)
    }

    fun onRefresh() = refresh(initial = false)

    fun onRetry() = refresh(initial = true)

    fun onNotificationsToggle(enabled: Boolean) {
        viewModelScope.launch { notificationsPreferences.setEnabled(enabled) }
    }

    fun onClaim(promoId: Long) {
        val repo = repository ?: return
        viewModelScope.launch { repo.markClaimed(promoId) }
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
        claimed: Set<Long>,
        current: Inputs,
    ): PromosUiState {
        val error = current.error
        return when {
            error != null && promos.isEmpty() -> PromosUiState.Error(error)
            current.isInitialLoad && promos.isEmpty() -> PromosUiState.Loading
            else -> content(promos, claimed, current)
        }
    }

    private fun content(
        promos: List<Promo>,
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
                    isClaimed = promo.id in claimed,
                )
            },
            query = current.query,
            tab = current.tab,
            counts = PromoFilter.counts(promos),
            expandedId = current.expandedId,
            isRefreshing = current.isRefreshing,
            isStale = current.isStale,
        )
    }

    private companion object {
        const val TICK_INTERVAL_MILLIS = 1_000L
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
    }
}
