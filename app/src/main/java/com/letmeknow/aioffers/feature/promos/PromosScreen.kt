package com.letmeknow.aioffers.feature.promos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.letmeknow.aioffers.feature.alerts.AlertsDestination
import com.letmeknow.aioffers.feature.alerts.rememberNotificationPermission
import com.letmeknow.aioffers.feature.promos.components.AuroraBackground
import com.letmeknow.aioffers.feature.promos.components.EmptyState
import com.letmeknow.aioffers.feature.promos.components.ErrorState
import com.letmeknow.aioffers.feature.promos.components.FadeUpItem
import com.letmeknow.aioffers.feature.promos.components.FilterTabs
import com.letmeknow.aioffers.feature.promos.components.Footer
import com.letmeknow.aioffers.feature.promos.components.Header
import com.letmeknow.aioffers.feature.promos.components.Hero
import com.letmeknow.aioffers.feature.promos.components.PromoCard
import com.letmeknow.aioffers.feature.promos.components.SkeletonCard
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.Motion
import com.letmeknow.aioffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.time.Duration
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

    /** Campana de la tarjeta: seguir o dejar de seguir para recibir avisos. */
    data class OnFollowToggle(val id: Long, val followed: Boolean) : PromosEvent

    /**
     * CTA "Reclamar". Lleva el modelo entero y no solo el id porque quien lo maneja necesita
     * el link externo además de registrar el reclamo.
     */
    data class OnClaim(val promo: PromoUiModel) : PromosEvent

    /** Reintentar desde la pantalla de error. */
    data object OnRetry : PromosEvent

    /** Campana del header: abrir el destino de avisos. */
    data object OnAlertsOpen : PromosEvent

    data object OnAlertsDismiss : PromosEvent
}

/**
 * Arma el fondo aurora, header, hero, filtros, grilla/estado y footer. Pull-to-refresh en
 * `isRefreshing`; contenedor centrado con ancho máximo [Dimens.MaxContentWidth] y grilla de
 * 1/2/3 columnas según [Dimens.TwoColumnBreakpoint]/[Dimens.ThreeColumnBreakpoint].
 *
 * El destino de avisos se monta acá porque su estado (`alerts.isOpen`) es parte del estado de
 * la pantalla; lo que hay dentro está encapsulado en `AlertsDestination`.
 */
@Composable
fun PromosScreen(
    state: PromosUiState,
    onEvent: (PromosEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val reduceMotion = LocalReduceMotion.current

    // El permiso de notificaciones no puede vivir en el ViewModel: `rememberLauncherForActivityResult`
    // necesita la composición. Se pide en contexto, en la campana de la tarjeta, nunca al
    // arrancar la pantalla — por eso acá solo se crea, no se dispara.
    val notificationPermission = rememberNotificationPermission()

    Box(modifier = modifier.fillMaxSize()) {
        // El aurora va a sangre, por debajo de las barras del sistema: es fondo, no contenido.
        AuroraBackground(hazeState = hazeState, modifier = Modifier.fillMaxSize())

        // El contenido sí respeta los insets. Sin esto, `enableEdgeToEdge()` deja el header
        // pisado por la barra de estado — no se nota en el emulador, sí en un teléfono real.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Header(
                hazeState = hazeState,
                onAlertsClick = { onEvent(PromosEvent.OnAlertsOpen) },
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
                                        FadeUpItem(delayMillis = Motion.staggerDelay(index, reduceMotion)) {
                                            PromoCard(
                                                promo = promo,
                                                isExpanded = state.expandedId == promo.promo.id,
                                                hazeState = hazeState,
                                                onToggleExpand = {
                                                    onEvent(PromosEvent.OnCardToggle(promo.promo.id))
                                                },
                                                onToggleFollow = {
                                                    val followed = !promo.isFollowed
                                                    // Marcar una oferta es el momento en que
                                                    // el permiso tiene un motivo entendible.
                                                    // Si lo niega, el seguimiento se guarda
                                                    // igual y no se vuelve a preguntar.
                                                    if (followed) notificationPermission.requestOnce()
                                                    onEvent(
                                                        PromosEvent.OnFollowToggle(
                                                            id = promo.promo.id,
                                                            followed = followed,
                                                        ),
                                                    )
                                                },
                                                onClaim = { onEvent(PromosEvent.OnClaim(promo)) },
                                                remainingSeconds = promo.remainingSecondsOrNull(),
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

        if (state is PromosUiState.Content) {
            AlertsDestination(
                state = state.alerts,
                onDismiss = { onEvent(PromosEvent.OnAlertsDismiss) },
                onUnfollow = { id -> onEvent(PromosEvent.OnFollowToggle(id = id, followed = false)) },
            )
        }
    }
}

private fun LazyGridScope.fullWidthItem(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

/**
 * Segundos que faltan para el vencimiento, solo para ofertas urgentes.
 *
 * Se resuelve acá y no dentro de la tarjeta a propósito: el ViewModel reemite el estado cada
 * segundo mientras haya al menos una oferta urgente, así que este `Instant.now()` se refresca
 * con ese único tick en vez de con un ticker por tarjeta visible. Nunca devuelve negativos:
 * el countdown se queda en cero cuando la fecha ya pasó.
 */
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
