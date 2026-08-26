package com.letmeknow.aioffers.feature.promos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letmeknow.aioffers.domain.model.ExpirationState
import com.letmeknow.aioffers.feature.promos.components.AuroraBackground
import com.letmeknow.aioffers.feature.promos.components.Header
import com.letmeknow.aioffers.feature.promos.components.Hero
import com.letmeknow.aioffers.feature.promos.components.PromoCard
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.glassSource
import dev.chrisbanes.haze.HazeState
import java.time.Duration
import java.time.Instant

/**
 * ANDAMIO TEMPORAL — borrar cuando se mergee feat/ui-shell.
 *
 * Ya monta la [PromoCard] real, pero le falta todo lo que rodea a la grilla: fondo aurora,
 * header, hero con buscador, pills de filtro, skeletons y footer. Eso llega con la etapa 2.
 *
 * El vidrio de las tarjetas todavía no se luce porque detrás solo hay un color plano: haze
 * está difuminando un fondo liso. Cuando entre el aurora animado, el efecto aparece sin
 * tocar la tarjeta.
 */
@Composable
fun MinimalPromosScreen(
    state: PromosUiState,
    onRetry: () -> Unit,
    onCardClick: (Long) -> Unit,
    onFollowToggle: (Long, Boolean) -> Unit,
    onClaim: (PromoUiModel) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = remember { HazeState() }

    Box(modifier.fillMaxSize()) {
        // Fondo fijo: no scrollea con el contenido, y es la fuente del blur de todo el vidrio.
        AuroraBackground(hazeState = hazeState)

        Column(Modifier.fillMaxSize()) {
            Header(hazeState = hazeState, onAlertsClick = {})

            Column(Modifier.padding(horizontal = Dimens.ScreenPadding)) {
                Hero(
                    verifiedCount = (state as? PromosUiState.Content)?.counts?.all ?: 0,
                    query = (state as? PromosUiState.Content)?.query.orEmpty(),
                    onQueryChange = onQueryChange,
                    hazeState = hazeState,
                )
                Spacer(Modifier.height(16.dp))

            when (state) {
                PromosUiState.Loading -> Centered {
                    CircularProgressIndicator(color = AppColors.Indigo500)
                }

                is PromosUiState.Error -> Centered {
                    Column {
                        Text(
                            text = "No se pudieron cargar las ofertas",
                            color = AppColors.OnBackground,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage(state.kind), color = AppColors.OnBackgroundMuted)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Reintentar",
                            color = AppColors.TextGradientStart,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onRetry),
                        )
                    }
                }

                is PromosUiState.Content -> if (state.isEmpty) {
                    Centered { Text("No hay ofertas.", color = AppColors.OnBackgroundMuted) }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                        contentPadding = PaddingValues(bottom = 32.dp),
                    ) {
                        items(state.promos, key = { it.promo.id }) { model ->
                            PromoCard(
                                promo = model,
                                isExpanded = state.expandedId == model.promo.id,
                                hazeState = hazeState,
                                onToggleExpand = { onCardClick(model.promo.id) },
                                onToggleFollow = {
                                    onFollowToggle(model.promo.id, !model.isFollowed)
                                },
                                onClaim = { onClaim(model) },
                                remainingSeconds = model.remainingSecondsOrNull(),
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

/**
 * Segundos que faltan para el vencimiento, solo para ofertas urgentes.
 *
 * Se calcula acá y no en la tarjeta a propósito: el ViewModel reemite el estado cada segundo
 * mientras haya al menos una oferta urgente, así que este `Instant.now()` se refresca con ese
 * único tick en vez de con un ticker por tarjeta. Nunca devuelve negativos: el countdown se
 * queda en cero cuando la fecha ya pasó.
 */
private fun PromoUiModel.remainingSecondsOrNull(): Long? {
    if (state != ExpirationState.URGENT) return null
    val expiresAt = promo.expiresAt ?: return null
    return Duration.between(Instant.now(), expiresAt).seconds.coerceAtLeast(0L)
}

private fun errorMessage(kind: ErrorKind): String = when (kind) {
    is ErrorKind.MissingConfig ->
        "Falta configurar ${kind.missingKeys.joinToString(" y ")} en local.properties."
    ErrorKind.Network -> "Sin conexión. Revisá tu red e intentá de nuevo."
    is ErrorKind.Http -> "El servidor respondió ${kind.code}."
    ErrorKind.MalformedPayload -> "La respuesta del servidor no tiene el formato esperado."
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}
