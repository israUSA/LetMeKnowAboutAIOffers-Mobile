package com.letmeknow.studentoffers.feature.promos

import androidx.compose.runtime.Immutable
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo

enum class PromoTab { ALL, PERMANENT, LIMITED }

sealed interface ErrorKind {
    data class MissingConfig(val missingKeys: List<String>) : ErrorKind

    data object Network : ErrorKind

    data class Http(val code: Int) : ErrorKind

    data object MalformedPayload : ErrorKind
}

@Immutable
data class PromoUiModel(
    val promo: Promo,
    val state: ExpirationState,
    val timeRemainingPercent: Float?,
    val expirationLabel: String,
    val isClaimed: Boolean,
)

@Immutable
data class TabCounts(val all: Int, val permanent: Int, val limited: Int)

sealed interface PromosUiState {

    data object Loading : PromosUiState

    data class Error(val kind: ErrorKind) : PromosUiState

    @Immutable
    data class Content(
        val promos: List<PromoUiModel>,
        val query: String,
        val tab: PromoTab,
        val counts: TabCounts,
        val expandedId: Long?,
        val isRefreshing: Boolean,
        val isStale: Boolean,
    ) : PromosUiState {
        val isEmpty: Boolean get() = promos.isEmpty()
    }
}
