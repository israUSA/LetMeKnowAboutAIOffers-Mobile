package com.letmeknow.aioffers.feature.promos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.core.ui.LocalReduceMotion
import com.letmeknow.aioffers.domain.model.ExpirationState
import com.letmeknow.aioffers.domain.model.Promo
import com.letmeknow.aioffers.feature.promos.components.AuroraBackground
import com.letmeknow.aioffers.feature.promos.components.EmptyState
import com.letmeknow.aioffers.feature.promos.components.ErrorState
import com.letmeknow.aioffers.feature.promos.components.FadeUpItem
import com.letmeknow.aioffers.feature.promos.components.FilterTabs
import com.letmeknow.aioffers.feature.promos.components.Footer
import com.letmeknow.aioffers.feature.promos.components.Header
import com.letmeknow.aioffers.feature.promos.components.Hero
import com.letmeknow.aioffers.feature.promos.components.SkeletonCard
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.Motion
import com.letmeknow.aioffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.time.Instant

/** Cantidad de skeletons a mostrar mientras carga (no hay conteo real todavía). */
private const val LoadingSkeletonCount = 6

/**
 * Eventos que la pantalla puede emitir hacia el `PromosViewModel` (dueño de query, tab,
 * `expandedId` y refresh). `PromosScreen` es stateless: recibe `(state, onEvent)`.
 */
sealed interface PromosEvent {
    data class OnQueryChange(val query: String) : PromosEvent
    data class OnTabSelected(val tab: PromoTab) : PromosEvent
    data object OnRefresh : PromosEvent
    data class OnCardToggle(val id: Long) : PromosEvent
}

/**
 * Arma el fondo aurora, header, hero, filtros, grilla/estado y footer. Pull-to-refresh en
 * `isRefreshing`; contenedor centrado con ancho máximo [Dimens.MaxContentWidth] y grilla de
 * 1/2/3 columnas según [Dimens.TwoColumnBreakpoint]/[Dimens.ThreeColumnBreakpoint].
 *
 * El bottom sheet de avisos lo implementa otro agente en wave 2: acá [onAlertsClick] queda
 * sin implementación real. Donde iría la tarjeta real hay un placeholder simple
 * (TODO(feat/promo-card)) — el merge lo resuelve el coordinador.
 */
@Composable
fun PromosScreen(
    state: PromosUiState,
    onEvent: (PromosEvent) -> Unit,
    onAlertsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val reduceMotion = LocalReduceMotion.current

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackground(hazeState = hazeState, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                hazeState = hazeState,
                onAlertsClick = onAlertsClick,
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
                                fullWidthItem { ErrorState(kind = state.kind, hazeState = hazeState) }
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
                                        FadeUpItem(delayMillis = Motion.staggerDelay(index, reduceMotion)) {
                                            PromoCardPlaceholder(
                                                promo = promo,
                                                hazeState = hazeState,
                                                onClick = { onEvent(PromosEvent.OnCardToggle(promo.promo.id)) },
                                            )
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
    }
}

private fun LazyGridScope.fullWidthItem(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

/**
 * TODO(feat/promo-card): reemplazar por la tarjeta real (la implementa otro agente en
 * paralelo). Placeholder simple con la info mínima para verificar el layout de la grilla.
 */
@Composable
private fun PromoCardPlaceholder(promo: PromoUiModel, hazeState: HazeState, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(state = hazeState, shape = Dimens.Shape2xl)
            .clickable(onClick = onClick)
            .padding(Dimens.CardPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = promo.promo.company, style = MaterialTheme.typography.labelSmall)
        Text(text = promo.promo.title, style = MaterialTheme.typography.titleMedium)
        Text(text = promo.expirationLabel, style = MaterialTheme.typography.bodySmall)
    }
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
    isFollowed = false,
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
            onEvent = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true, backgroundColor = 0xFF060610, widthDp = 380, heightDp = 800)
@Composable
private fun PromosScreenLoadingPreview() {
    AppTheme {
        PromosScreen(state = PromosUiState.Loading, onEvent = {})
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
            onEvent = {},
        )
    }
}
