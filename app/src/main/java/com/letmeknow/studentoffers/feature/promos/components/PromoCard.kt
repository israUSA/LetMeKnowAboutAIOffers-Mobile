package com.letmeknow.studentoffers.feature.promos.components

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
import androidx.compose.material3.Icon
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
import com.letmeknow.studentoffers.core.ui.LocalReduceMotion
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.feature.promos.PromoUiModel
import com.letmeknow.studentoffers.ui.theme.AppColors
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.BrandGradient
import com.letmeknow.studentoffers.ui.theme.CompanyLabelStyle
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.Motion
import com.letmeknow.studentoffers.ui.theme.SpaceGroteskFamily
import com.letmeknow.studentoffers.ui.theme.colors
import com.letmeknow.studentoffers.ui.theme.glassSource
import com.letmeknow.studentoffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.time.Instant

private const val ExpandActionLabel = "Ver la descripción completa"
private const val CollapseActionLabel = "Ocultar la descripción"
private const val ExpandedStateDescription = "Expandida"
private const val CollapsedStateDescription = "Colapsada"
private const val ClaimText = "Reclamar"
private const val ClaimActionLabel = "Abrir la oferta en el navegador"

private val CtaIconSize = 18.dp

@Composable
fun PromoCard(
    promo: PromoUiModel,
    isExpanded: Boolean,
    hazeState: HazeState,
    onToggleExpand: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier,
    remainingSeconds: Long? = null,
) {
    val reduceMotion = LocalReduceMotion.current
    val stateColors = promo.state.colors

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
            .semantics {
                stateDescription =
                    if (isExpanded) ExpandedStateDescription else CollapsedStateDescription
            },
    ) {
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

                ExpandChevron(rotation = chevronRotation)
            }

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
            modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
        )
    }
}

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
            contentDescription = null,
            tint = AppColors.OnBackground,
            modifier = Modifier.size(CtaIconSize),
        )
    }
}

private val PreviewNow: Instant = Instant.parse("2026-08-26T12:00:00Z")

private const val PreviewUrgentSeconds = 3L * 24 * 60 * 60 + 4 * 60 * 60 + 7 * 60 + 33

private fun previewPromo(state: ExpirationState): PromoUiModel {
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
            description = "Verifica tu correo institucional y obtén acceso completo mientras " +
                "seas estudiante. La verificación puede tardar unos días y hay que renovarla " +
                "cada año, así que conviene iniciar el trámite con tiempo.",
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
        isClaimed = false,
    )
}

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
                promo = previewPromo(state),
                isExpanded = false,
                hazeState = hazeState,
                onToggleExpand = {},
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
                promo = previewPromo(state),
                isExpanded = true,
                hazeState = hazeState,
                onToggleExpand = {},
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
                promo = previewPromo(ExpirationState.URGENT),
                isExpanded = expanded,
                hazeState = hazeState,
                onToggleExpand = {},
                onClaim = {},
                remainingSeconds = if (expanded) -120L else PreviewUrgentSeconds,
            )
        }
    }
}
