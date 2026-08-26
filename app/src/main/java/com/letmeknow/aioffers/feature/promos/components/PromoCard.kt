package com.letmeknow.aioffers.feature.promos.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.core.ui.LocalReduceMotion
import com.letmeknow.aioffers.domain.model.ExpirationState
import com.letmeknow.aioffers.domain.model.Promo
import com.letmeknow.aioffers.feature.promos.PromoUiModel
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.BrandGradient
import com.letmeknow.aioffers.ui.theme.CompanyLabelStyle
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.Motion
import com.letmeknow.aioffers.ui.theme.SpaceGroteskFamily
import com.letmeknow.aioffers.ui.theme.colors
import com.letmeknow.aioffers.ui.theme.glassSource
import com.letmeknow.aioffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.time.Instant

private const val ExpandActionLabel = "Ver la descripción completa"
private const val CollapseActionLabel = "Ocultar la descripción"
private const val ExpandedStateDescription = "Expandida"
private const val CollapsedStateDescription = "Colapsada"
private const val FollowContentDescription = "Avisarme de esta oferta"
private const val FollowingStateDescription = "Siguiendo"
private const val NotFollowingStateDescription = "No seguida"
private const val ClaimText = "Reclamar"
private const val ClaimActionLabel = "Abrir la oferta en el navegador"

/** Tamaño del ícono de enlace externo del CTA, proporcionado al texto del botón. */
private val CtaIconSize = 18.dp

/**
 * La tarjeta de oferta: la interacción central de la app.
 *
 * **Es stateless a propósito.** DESIGN_SYSTEM.md exige que solo una tarjeta pueda estar
 * expandida a la vez en toda la grilla, y eso solo se garantiza con un único dueño del estado:
 * `expandedId` vive en `PromosViewModel`. Si la tarjeta recordara su propio `isExpanded`, nada
 * impediría tener dos abiertas.
 *
 * @param promo modelo ya resuelto: estado, porcentaje y texto de expiración vienen calculados.
 * @param isExpanded lo decide el dueño de la grilla comparando con `expandedId`.
 * @param hazeState el `HazeState` de la pantalla, cuyo source es el fondo aurora.
 * @param remainingSeconds segundos hasta el vencimiento, solo relevante cuando el estado es
 *   [ExpirationState.URGENT]. Lo recalcula la pantalla una vez por segundo para toda la grilla;
 *   en el resto de los estados llega `null` y la tarjeta sigue siendo skippable aunque el tick
 *   siga corriendo.
 */
@Composable
fun PromoCard(
    promo: PromoUiModel,
    isExpanded: Boolean,
    hazeState: HazeState,
    onToggleExpand: () -> Unit,
    onToggleFollow: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier,
    remainingSeconds: Long? = null,
) {
    val reduceMotion = LocalReduceMotion.current
    val stateColors = promo.state.colors

    // Foco visible para teclado y D-pad: el borde de vidrio se enciende con el color del estado.
    // `clickable` ya hace focusable a la tarjeta, así que no hace falta un `focusable()` extra.
    var isFocused by remember { mutableStateOf(false) }

    val chevronRotation = animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(Motion.durationOrInstant(Motion.ChevronMillis, reduceMotion)),
        label = "chevron-rotation",
    )
    val expandMillis = Motion.durationOrInstant(Motion.ExpandMillis, reduceMotion)

    Column(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .glassSurface(
                state = hazeState,
                shape = Dimens.Shape2xl,
                tint = if (isExpanded) AppColors.GlassSurfaceHigh else AppColors.GlassSurfaceMid,
                border = if (isFocused) stateColors.text else AppColors.GlassBorderLow,
            )
            .clickable(
                onClickLabel = if (isExpanded) CollapseActionLabel else ExpandActionLabel,
                role = Role.Button,
                onClick = onToggleExpand,
            )
            // El estado se anuncia explícitamente; no alcanza con que el chevron rote.
            .semantics {
                stateDescription =
                    if (isExpanded) ExpandedStateDescription else CollapsedStateDescription
            },
    ) {
        // Solo las ofertas con vencimiento tienen barra de progreso.
        promo.timeRemainingPercent?.let { percent ->
            TimeProgressBar(percent = percent, state = promo.state)
        }

        Column(Modifier.padding(Dimens.CardPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            ) {
                CompanyAvatar(company = promo.promo.company)

                Column(Modifier.weight(1f)) {
                    Text(
                        text = promo.promo.company.uppercase(),
                        style = CompanyLabelStyle,
                        color = AppColors.OnBackgroundMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = promo.promo.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = AppColors.OnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                FollowBell(isFollowed = promo.isFollowed, onToggleFollow = onToggleFollow)
                ExpandChevron(rotation = chevronRotation)
            }

            // Colapsada, la descripción NO existe en el árbol: no hay preview ni truncado.
            // `AnimatedVisibility` crece en alto y hace fade-in a la vez, nunca un salto seco.
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(expandMillis, easing = Motion.EaseInOut)) +
                    fadeIn(tween(expandMillis, easing = Motion.EaseInOut)),
                exit = shrinkVertically(tween(expandMillis, easing = Motion.EaseInOut)) +
                    fadeOut(tween(expandMillis, easing = Motion.EaseInOut)),
            ) {
                Text(
                    text = promo.promo.description,
                    modifier = Modifier.padding(top = Dimens.CardSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.OnBackgroundMuted,
                )
            }

            Spacer(Modifier.height(Dimens.CardSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // El envoltorio con `weight` es el que se estira; la pastilla conserva su ancho
                // natural y queda pegada a la izquierda, con el CTA empujado al borde derecho.
                Box(Modifier.weight(1f)) {
                    if (promo.state == ExpirationState.URGENT) {
                        CountdownTimer(remainingSeconds = remainingSeconds ?: 0L)
                    } else {
                        ExpirationBadge(label = promo.expirationLabel, state = promo.state)
                    }
                }

                ClaimButton(onClaim = onClaim)
            }
        }
    }
}

/**
 * Campana de seguimiento. Es un `IconToggleButton` y no un `IconButton` para que TalkBack lo
 * anuncie como conmutador; encima se le agrega el `stateDescription` explícito.
 *
 * El click no se propaga a la tarjeta: el `clickable` interno consume el evento de puntero antes
 * de que llegue al de la tarjeta, así que seguir una oferta nunca la expande.
 */
@Composable
private fun FollowBell(
    isFollowed: Boolean,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconToggleButton(
        checked = isFollowed,
        onCheckedChange = { onToggleFollow() },
        modifier = modifier
            .defaultMinSize(
                minWidth = Dimens.MinTouchTarget,
                minHeight = Dimens.MinTouchTarget,
            )
            .semantics {
                stateDescription =
                    if (isFollowed) FollowingStateDescription else NotFollowingStateDescription
            },
    ) {
        Icon(
            imageVector = if (isFollowed) {
                Icons.Rounded.NotificationsActive
            } else {
                Icons.Rounded.NotificationsNone
            },
            contentDescription = FollowContentDescription,
            tint = if (isFollowed) AppColors.Fuchsia500 else AppColors.OnBackgroundMuted,
        )
    }
}

/**
 * Chevron que rota 180 grados al expandir.
 *
 * No es interactivo ni accesible: la tarjeta entera ya es el botón y su `stateDescription` ya
 * anuncia expandido/colapsado. Un nodo más acá sería ruido puro para el lector de pantalla, así
 * que se limpia con `clearAndSetSemantics {}`. Mantiene igual el área de 44dp para que la fila
 * superior conserve el ritmo del resto de los controles.
 */
@Composable
private fun ExpandChevron(
    rotation: State<Float>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(Dimens.MinTouchTarget)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = AppColors.OnBackgroundMuted,
            // Lectura diferida: la rotación se resuelve en la fase de dibujo, sin recomponer.
            modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
        )
    }
}

/** CTA "Reclamar" con el gradiente de marca. Tampoco propaga el click a la tarjeta. */
@Composable
private fun ClaimButton(
    onClaim: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .clip(Dimens.ShapePill)
            .background(BrandGradient)
            .border(
                width = 1.dp,
                color = if (isFocused) AppColors.OnBackground else Color.Transparent,
                shape = Dimens.ShapePill,
            )
            .clickable(
                onClickLabel = ClaimActionLabel,
                role = Role.Button,
                onClick = onClaim,
            )
            .defaultMinSize(
                minWidth = Dimens.MinTouchTarget,
                minHeight = Dimens.MinTouchTarget,
            )
            .padding(horizontal = Dimens.CardPadding, vertical = Dimens.CardSpacing / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing / 2),
    ) {
        Text(
            text = ClaimText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.OnBackground,
            maxLines = 1,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            // El texto del botón ya dice qué hace; el ícono no agrega información nueva.
            contentDescription = null,
            tint = AppColors.OnBackground,
            modifier = Modifier.size(CtaIconSize),
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Previews. Datos falsos: la tarjeta no depende de que data/ ni el ViewModel estén listos.
// ---------------------------------------------------------------------------------------------

private val PreviewNow: Instant = Instant.parse("2026-08-26T12:00:00Z")

private const val PreviewUrgentSeconds = 3L * 24 * 60 * 60 + 4 * 60 * 60 + 7 * 60 + 33

private fun previewPromo(
    state: ExpirationState,
    isFollowed: Boolean = false,
): PromoUiModel {
    val (company, title, days) = when (state) {
        ExpirationState.URGENT ->
            Triple("GitHub Education", "Copilot Pro gratis para estudiantes", 3L)

        ExpirationState.WARNING ->
            Triple("JetBrains", "Licencia completa del All Products Pack", 21L)

        ExpirationState.COMFORTABLE ->
            Triple("Microsoft Azure", "100 USD de crédito sin tarjeta", 120L)

        ExpirationState.PERMANENT ->
            Triple("Figma", "Plan Education para equipos de clase", 0L)
    }
    val expiresAt = if (state == ExpirationState.PERMANENT) {
        null
    } else {
        PreviewNow.plusSeconds(days * 24 * 60 * 60)
    }
    return PromoUiModel(
        promo = Promo(
            id = state.ordinal.toLong(),
            company = company,
            title = title,
            description = "Verificá tu correo institucional y obtené acceso completo mientras " +
                "seas estudiante. La verificación puede tardar unos días y hay que renovarla " +
                "cada año, así que conviene arrancar el trámite con tiempo.",
            reclaimLink = "https://example.com/oferta",
            createdAt = PreviewNow.minusSeconds(30L * 24 * 60 * 60),
            expiresAt = expiresAt,
        ),
        state = state,
        timeRemainingPercent = when (state) {
            ExpirationState.URGENT -> 8f
            ExpirationState.WARNING -> 32f
            ExpirationState.COMFORTABLE -> 78f
            ExpirationState.PERMANENT -> null
        },
        expirationLabel = when (state) {
            ExpirationState.URGENT -> ""
            ExpirationState.WARNING -> "Expira en 21 días"
            ExpirationState.COMFORTABLE -> "Expira el 24 dic 2026"
            ExpirationState.PERMANENT -> "Siempre disponible"
        },
        isFollowed = isFollowed,
        isClaimed = false,
    )
}

/**
 * El `hazeEffect` de la tarjeta necesita un `hazeSource` detrás para tener algo que difuminar.
 * En la app ese source es el fondo aurora; acá alcanza con un panel plano del color de fondo.
 */
@Composable
private fun PromoCardPreviewScaffold(content: @Composable (HazeState) -> Unit) {
    AppTheme {
        val hazeState = rememberHazeState()
        Box(Modifier.background(AppColors.Background)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .glassSource(hazeState)
                    .background(AppColors.Background),
            )
            Column(
                modifier = Modifier.padding(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            ) {
                content(hazeState)
            }
        }
    }
}

@Preview(
    name = "Colapsada - los cuatro estados",
    widthDp = 400,
    heightDp = 640,
    showBackground = true,
    backgroundColor = 0xFF060610,
)
@Composable
private fun PromoCardCollapsedPreview() {
    PromoCardPreviewScaffold { hazeState ->
        ExpirationState.entries.forEach { state ->
            PromoCard(
                promo = previewPromo(state, isFollowed = state == ExpirationState.WARNING),
                isExpanded = false,
                hazeState = hazeState,
                onToggleExpand = {},
                onToggleFollow = {},
                onClaim = {},
                remainingSeconds = PreviewUrgentSeconds,
            )
        }
    }
}

@Preview(
    name = "Expandida - los cuatro estados",
    widthDp = 400,
    heightDp = 1120,
    showBackground = true,
    backgroundColor = 0xFF060610,
)
@Composable
private fun PromoCardExpandedPreview() {
    PromoCardPreviewScaffold { hazeState ->
        ExpirationState.entries.forEach { state ->
            PromoCard(
                promo = previewPromo(state, isFollowed = state == ExpirationState.URGENT),
                isExpanded = true,
                hazeState = hazeState,
                onToggleExpand = {},
                onToggleFollow = {},
                onClaim = {},
                remainingSeconds = PreviewUrgentSeconds,
            )
        }
    }
}

@Preview(
    name = "Urgente - colapsada vs expandida",
    widthDp = 400,
    heightDp = 540,
    showBackground = true,
    backgroundColor = 0xFF060610,
)
@Composable
private fun PromoCardUrgentPreview() {
    PromoCardPreviewScaffold { hazeState ->
        listOf(false, true).forEach { expanded ->
            PromoCard(
                promo = previewPromo(ExpirationState.URGENT, isFollowed = expanded),
                isExpanded = expanded,
                hazeState = hazeState,
                onToggleExpand = {},
                onToggleFollow = {},
                onClaim = {},
                // Con `expanded` se fuerza un valor ya vencido: el countdown muestra ceros.
                remainingSeconds = if (expanded) -120L else PreviewUrgentSeconds,
            )
        }
    }
}
