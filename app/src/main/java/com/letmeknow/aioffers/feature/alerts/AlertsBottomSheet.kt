package com.letmeknow.aioffers.feature.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.aioffers.R
import com.letmeknow.aioffers.domain.model.ExpirationState
import com.letmeknow.aioffers.feature.promos.components.CompanyAvatar
import com.letmeknow.aioffers.feature.promos.components.ExpirationBadge
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme
import com.letmeknow.aioffers.ui.theme.BrandGradient
import com.letmeknow.aioffers.ui.theme.Dimens
import com.letmeknow.aioffers.ui.theme.glassSource
import com.letmeknow.aioffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Destino de avisos. **Es el único punto de entrada**: la pantalla lo llama y no sabe que
 * abajo hay un `ModalBottomSheet`.
 *
 * Esa indirección es lo que pide "encapsular el destino". El día que la app tenga Navigation
 * Compose, este composable pasa a ser una entrada de ruta (`bottomSheet("alerts")`) y
 * `PromosScreen` no cambia una línea. Hoy el back stack es un `Boolean` en el estado.
 *
 * El permiso se resuelve acá adentro y no en la pantalla: el sheet es el único lugar que lo
 * muestra y el único que ofrece activarlo.
 */
@Composable
fun AlertsDestination(
    state: AlertsUiState,
    onDismiss: () -> Unit,
    onUnfollow: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isOpen) return

    val permission = rememberNotificationPermission()

    AlertsBottomSheet(
        alerts = state.alerts,
        notificationsEnabled = permission.isGranted,
        onDismiss = onDismiss,
        onUnfollow = onUnfollow,
        onEnableNotifications = permission::request,
        modifier = modifier,
    )
}

/**
 * Las ofertas que el usuario sigue, con su estado de reclamo.
 *
 * Stateless salvo por el `SheetState`, que es un objeto de runtime de Material3 y no puede
 * vivir fuera de la composición.
 *
 * @param notificationsEnabled si falta el permiso se muestra el aviso para activarlo. Que
 *   falte no vacía la lista: el seguimiento se guardó igual, solo no llegan avisos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsBottomSheet(
    alerts: List<AlertUiModel>,
    notificationsEnabled: Boolean,
    onDismiss: () -> Unit,
    onUnfollow: (Long) -> Unit,
    onEnableNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val hazeState = rememberHazeState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        contentColor = AppColors.OnBackground,
    ) {
        // El sheet vive en su propia ventana, así que el aurora de la pantalla no está detrás
        // para difuminar. Se le da un `hazeSource` propio del color de fondo: las superficies
        // quedan con el mismo tinte y el mismo borde de vidrio que el resto de la app, que es
        // lo que importa para que se lea como la misma pieza.
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .glassSource(hazeState)
                    .background(AppColors.Background),
            )
            SheetContent(
                alerts = alerts,
                notificationsEnabled = notificationsEnabled,
                hazeState = hazeState,
                onUnfollow = onUnfollow,
                onEnableNotifications = onEnableNotifications,
            )
        }
    }
}

@Composable
private fun SheetContent(
    alerts: List<AlertUiModel>,
    notificationsEnabled: Boolean,
    hazeState: HazeState,
    onUnfollow: (Long) -> Unit,
    onEnableNotifications: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding)
            .padding(bottom = Dimens.SectionSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
    ) {
        Text(
            text = stringResource(R.string.alerts_sheet_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.alerts_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnBackgroundMuted,
        )

        if (!notificationsEnabled) {
            PermissionCard(hazeState = hazeState, onEnableNotifications = onEnableNotifications)
        }

        if (alerts.isEmpty()) {
            AlertsEmptyState(hazeState = hazeState)
        } else {
            LazyColumn(
                // Tope de alto para que una lista larga no empuje el sheet a pantalla completa
                // sin que el usuario lo haya arrastrado hasta ahí.
                modifier = Modifier.heightIn(max = ListMaxHeight),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            ) {
                items(items = alerts, key = AlertUiModel::id) { alert ->
                    AlertRow(
                        alert = alert,
                        hazeState = hazeState,
                        onUnfollow = { onUnfollow(alert.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertRow(
    alert: AlertUiModel,
    hazeState: HazeState,
    onUnfollow: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(state = hazeState, shape = Dimens.Shape2xl)
            .padding(Dimens.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
    ) {
        CompanyAvatar(company = alert.company)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExpirationBadge(label = alert.expirationLabel, state = alert.state)
                Text(
                    text = stringResource(
                        if (alert.isClaimed) {
                            R.string.alerts_status_claimed
                        } else {
                            R.string.alerts_status_pending
                        },
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.OnBackgroundMuted,
                    maxLines = 1,
                )
            }
        }

        IconButton(onClick = onUnfollow, modifier = Modifier.size(Dimens.MinTouchTarget)) {
            Icon(
                imageVector = Icons.Rounded.NotificationsOff,
                contentDescription = stringResource(R.string.alerts_unfollow, alert.title),
                tint = AppColors.OnBackgroundMuted,
            )
        }
    }
}

/**
 * Solo aparece si falta el permiso. No bloquea nada: es una oferta de activarlo, no un muro.
 *
 * Si el usuario ya negó el permiso dos veces, Android deja de mostrar el diálogo y este botón
 * no hace nada visible. Mandarlo a Ajustes sería insistir por otra puerta, que es justamente
 * lo que la regla de "no insistir" pide evitar.
 */
@Composable
private fun PermissionCard(hazeState: HazeState, onEnableNotifications: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(
                state = hazeState,
                shape = Dimens.Shape2xl,
                border = AppColors.GlassBorderMid,
            )
            .padding(Dimens.CardPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.alerts_permission_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = stringResource(R.string.alerts_permission_body),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.OnBackgroundMuted,
        )
        Text(
            text = stringResource(R.string.alerts_permission_action),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.OnBackground,
            modifier = Modifier
                .clip(Dimens.ShapePill)
                .background(BrandGradient)
                .clickable(role = Role.Button, onClick = onEnableNotifications)
                .defaultMinSize(minHeight = Dimens.MinTouchTarget)
                .padding(horizontal = Dimens.CardPadding, vertical = 12.dp),
        )
    }
}

@Composable
private fun AlertsEmptyState(hazeState: HazeState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(state = hazeState, shape = Dimens.Shape3xl)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.NotificationsNone,
            contentDescription = null,
            tint = AppColors.OnBackgroundMuted,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.alerts_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.alerts_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnBackgroundMuted,
            textAlign = TextAlign.Center,
        )
    }
}

private val ListMaxHeight = 420.dp

// ---------------------------------------------------------------------------------------------
// Previews. `ModalBottomSheet` necesita una ventana propia y no renderiza en un preview, así
// que se previsualiza el contenido, que es donde está todo el diseño.
// ---------------------------------------------------------------------------------------------

private fun previewAlert(
    id: Long,
    company: String,
    title: String,
    state: ExpirationState,
    label: String,
    claimed: Boolean = false,
) = AlertUiModel(
    id = id,
    company = company,
    title = title,
    state = state,
    expirationLabel = label,
    isClaimed = claimed,
)

@Composable
private fun AlertsPreviewScaffold(content: @Composable (HazeState) -> Unit) {
    AppTheme {
        val hazeState = rememberHazeState()
        Box(Modifier.background(AppColors.Background)) {
            Box(
                Modifier
                    .matchParentSize()
                    .glassSource(hazeState)
                    .background(AppColors.Background),
            )
            content(hazeState)
        }
    }
}

@Preview(name = "Con avisos", widthDp = 400, heightDp = 620, showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun AlertsSheetContentPreview() {
    AlertsPreviewScaffold { hazeState ->
        SheetContent(
            alerts = listOf(
                previewAlert(1, "GitHub Education", "Copilot Pro gratis", ExpirationState.URGENT, "Expira mañana"),
                previewAlert(2, "JetBrains", "All Products Pack", ExpirationState.WARNING, "Expira en 21 días", claimed = true),
                previewAlert(3, "Figma", "Plan Education", ExpirationState.PERMANENT, "Siempre disponible"),
            ),
            notificationsEnabled = true,
            hazeState = hazeState,
            onUnfollow = {},
            onEnableNotifications = {},
        )
    }
}

@Preview(name = "Sin permiso", widthDp = 400, heightDp = 620, showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun AlertsSheetNoPermissionPreview() {
    AlertsPreviewScaffold { hazeState ->
        SheetContent(
            alerts = listOf(
                previewAlert(1, "GitHub Education", "Copilot Pro gratis", ExpirationState.URGENT, "Expira mañana"),
            ),
            notificationsEnabled = false,
            hazeState = hazeState,
            onUnfollow = {},
            onEnableNotifications = {},
        )
    }
}

@Preview(name = "Vacío", widthDp = 400, heightDp = 420, showBackground = true, backgroundColor = 0xFF060610)
@Composable
private fun AlertsSheetEmptyPreview() {
    AlertsPreviewScaffold { hazeState ->
        SheetContent(
            alerts = emptyList(),
            notificationsEnabled = true,
            hazeState = hazeState,
            onUnfollow = {},
            onEnableNotifications = {},
        )
    }
}
