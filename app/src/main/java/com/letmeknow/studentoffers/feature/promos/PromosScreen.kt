package com.letmeknow.studentoffers.feature.promos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.studentoffers.R
import com.letmeknow.studentoffers.core.ui.LocalReduceMotion
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.feature.alerts.rememberNotificationPermission
import com.letmeknow.studentoffers.feature.promos.components.AuroraBackground
import com.letmeknow.studentoffers.feature.promos.components.EmptyState
import com.letmeknow.studentoffers.feature.promos.components.ErrorState
import com.letmeknow.studentoffers.feature.promos.components.FadeUpItem
import com.letmeknow.studentoffers.feature.promos.components.FilterTabs
import com.letmeknow.studentoffers.feature.promos.components.Footer
import com.letmeknow.studentoffers.feature.promos.components.Header
import com.letmeknow.studentoffers.feature.promos.components.Hero
import com.letmeknow.studentoffers.feature.promos.components.PromoCard
import com.letmeknow.studentoffers.feature.promos.components.SkeletonCard
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.Motion
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

private const val LoadingSkeletonCount = 6

sealed interface PromosEvent {
    data class OnQueryChange(val query: String) : PromosEvent
    data class OnTabSelected(val tab: PromoTab) : PromosEvent
    data object OnRefresh : PromosEvent
    data class OnCardToggle(val id: Long) : PromosEvent
    data class OnClaim(val promo: PromoUiModel) : PromosEvent
    data object OnRetry : PromosEvent
    data class OnNotificationsToggle(val enabled: Boolean) : PromosEvent
}

@Composable
fun PromosScreen(
    state: PromosUiState,
    notificationsEnabled: Boolean,
    onEvent: (PromosEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val reduceMotion = LocalReduceMotion.current

    val notificationPermission = rememberNotificationPermission()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val enabledMessage = stringResource(R.string.notifications_enabled_snackbar)
    val disabledMessage = stringResource(R.string.notifications_disabled_snackbar)

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackground(hazeState = hazeState, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Header(
                hazeState = hazeState,
                notificationsEnabled = notificationsEnabled,
                onNotificationsToggle = { enabled ->
                    if (enabled) notificationPermission.requestOnce()
                    onEvent(PromosEvent.OnNotificationsToggle(enabled))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (enabled) enabledMessage else disabledMessage,
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp),
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val columns = when {
                    maxWidth >= Dimens.ThreeColumnBreakpoint -> 3
                    maxWidth >= Dimens.TwoColumnBreakpoint -> 2
                    else -> 1
                }
                val horizontalPadding = if (maxWidth > Dimens.MaxContentWidth) {
                    (maxWidth - Dimens.MaxContentWidth) / 2 + Dimens.ScreenPadding
                } else {
                    Dimens.ScreenPadding
                }
                val isRefreshing = (state as? PromosUiState.Content)?.isRefreshing == true

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { onEvent(PromosEvent.OnRefresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = Dimens.ScreenPadding,
                            bottom = Dimens.SectionSpacing,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                    ) {
                        when (state) {
                            PromosUiState.Loading -> {
                                items(LoadingSkeletonCount) {
                                    SkeletonCard(hazeState = hazeState)
                                }
                            }

                            is PromosUiState.Error -> {
                                fullWidthItem {
                                    ErrorState(
                                        kind = state.kind,
                                        hazeState = hazeState,
                                        onRetry = { onEvent(PromosEvent.OnRetry) },
                                    )
                                }
                            }

                            is PromosUiState.Content -> {
                                fullWidthItem {
                                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)) {
                                        Hero(
                                            verifiedCount = state.counts.all,
                                            query = state.query,
                                            onQueryChange = { onEvent(PromosEvent.OnQueryChange(it)) },
                                            hazeState = hazeState,
                                        )
                                        FilterTabs(
                                            selected = state.tab,
                                            counts = state.counts,
                                            onTabSelected = { onEvent(PromosEvent.OnTabSelected(it)) },
                                            hazeState = hazeState,
                                        )
                                    }
                                }

                                if (state.isEmpty) {
                                    fullWidthItem { EmptyState(hazeState = hazeState) }
                                } else {
                                    itemsIndexed(
                                        items = state.promos,
                                        key = { _, promo -> promo.promo.id },
                                    ) { index, promo ->
                                        val card: @Composable () -> Unit = {
                                            PromoCard(
                                                promo = promo,
                                                isExpanded = state.expandedId == promo.promo.id,
                                                hazeState = hazeState,
                                                onToggleExpand = {
                                                    onEvent(PromosEvent.OnCardToggle(promo.promo.id))
                                                },
                                                onClaim = { onEvent(PromosEvent.OnClaim(promo)) },
                                                remainingSeconds = promo.remainingSecondsOrNull(),
                                            )
                                        }
                                        if (index <= Motion.StaggerItemLimit) {
                                            FadeUpItem(delayMillis = Motion.staggerDelay(index, reduceMotion)) {
                                                card()
                                            }
                                        } else {
                                            card()
                                        }
                                    }
                                }
                            }
                        }

                        fullWidthItem { Footer() }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(Dimens.ScreenPadding),
        )
    }
}

private fun LazyGridScope.fullWidthItem(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun PromoUiModel.remainingSecondsOrNull(): Long? {
    if (state != ExpirationState.URGENT) return null
    val expiresAt = promo.expiresAt ?: return null
    return Duration.between(Instant.now(), expiresAt).seconds.coerceAtLeast(0L)
}

private fun fakePromo(id: Long, company: String, title: String, permanent: Boolean) = PromoUiModel(
    promo = Promo(
        id = id,
        company = company,
        title = title,
        description = "Descripción de ejemplo para $title.",
        reclaimLink = "https://example.com",
        createdAt = Instant.now(),
        expiresAt = if (permanent) null else Instant.now().plusSeconds(86_400 * 10),
    ),
    state = if (permanent) ExpirationState.PERMANENT else ExpirationState.WARNING,
    timeRemainingPercent = if (permanent) null else 40f,
    expirationLabel = if (permanent) "Siempre disponible" else "Expira en 10 días",
    isClaimed = false,
)

@Preview(name = "Content", showBackground = true, backgroundColor = 0xFF060610, widthDp = 380, heightDp = 800)
@Composable
private fun PromosScreenContentPreview() {
    AppTheme {
        PromosScreen(
            state = PromosUiState.Content(
                promos = listOf(
                    fakePromo(1, "GitHub", "GitHub Student Pack", permanent = true),
                    fakePromo(2, "JetBrains", "Licencias gratuitas", permanent = false),
                    fakePromo(3, "Google", "Créditos de Cloud", permanent = false),
                ),
                query = "",
                tab = PromoTab.ALL,
                counts = TabCounts(all = 24, permanent = 9, limited = 15),
                expandedId = null,
                isRefreshing = false,
                isStale = false,
            ),
            notificationsEnabled = true,
            onEvent = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true, backgroundColor = 0xFF060610, widthDp = 380, heightDp = 800)
@Composable
private fun PromosScreenLoadingPreview() {
    AppTheme {
        PromosScreen(state = PromosUiState.Loading, notificationsEnabled = false, onEvent = {})
    }
}

@Preview(name = "Error", showBackground = true, backgroundColor = 0xFF060610, widthDp = 380, heightDp = 800)
@Composable
private fun PromosScreenErrorPreview() {
    AppTheme {
        PromosScreen(
            state = PromosUiState.Error(
                ErrorKind.MissingConfig(missingKeys = listOf("SUPABASE_URL", "SUPABASE_ANON_KEY")),
            ),
            notificationsEnabled = false,
            onEvent = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true, backgroundColor = 0xFF060610, widthDp = 380, heightDp = 800)
@Composable
private fun PromosScreenEmptyPreview() {
    AppTheme {
        PromosScreen(
            state = PromosUiState.Content(
                promos = emptyList(),
                query = "xyz",
                tab = PromoTab.ALL,
                counts = TabCounts(all = 24, permanent = 9, limited = 15),
                expandedId = null,
                isRefreshing = false,
                isStale = false,
            ),
            notificationsEnabled = false,
            onEvent = {},
        )
    }
}
