package com.letmeknow.studentoffers.feature.promos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letmeknow.studentoffers.R
import com.letmeknow.studentoffers.feature.promos.PromoTab
import com.letmeknow.studentoffers.feature.promos.TabCounts
import com.letmeknow.studentoffers.ui.theme.AppColors
import com.letmeknow.studentoffers.ui.theme.AppTheme
import com.letmeknow.studentoffers.ui.theme.BrandGradient
import com.letmeknow.studentoffers.ui.theme.Dimens
import com.letmeknow.studentoffers.ui.theme.glassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Fila de pills Todas / Permanentes / Por tiempo limitado. La pill activa lleva
 * [BrandGradient] de fondo; los contadores vienen ya calculados en [TabCounts].
 */
@Composable
fun FilterTabs(
    selected: PromoTab,
    counts: TabCounts,
    onTabSelected: (PromoTab) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    // En ancho de teléfono las tres pills no entran: sin scroll, la última se comprime, su
    // texto se parte en dos líneas y estira toda la fila hacia abajo. Con scroll horizontal
    // las etiquetas se mantienen completas y siguen siendo alcanzables deslizando.
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterTab(
            label = stringResource(R.string.filter_tab_all),
            icon = Icons.Filled.Apps,
            count = counts.all,
            isSelected = selected == PromoTab.ALL,
            onClick = { onTabSelected(PromoTab.ALL) },
            hazeState = hazeState,
        )
        FilterTab(
            label = stringResource(R.string.filter_tab_permanent),
            icon = Icons.Filled.AllInclusive,
            count = counts.permanent,
            isSelected = selected == PromoTab.PERMANENT,
            onClick = { onTabSelected(PromoTab.PERMANENT) },
            hazeState = hazeState,
        )
        FilterTab(
            label = stringResource(R.string.filter_tab_limited),
            icon = Icons.Filled.Schedule,
            count = counts.limited,
            isSelected = selected == PromoTab.LIMITED,
            onClick = { onTabSelected(PromoTab.LIMITED) },
            hazeState = hazeState,
        )
    }
}

@Composable
private fun FilterTab(
    label: String,
    icon: ImageVector,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    hazeState: HazeState,
) {
    val background: Modifier = if (isSelected) {
        Modifier
            .clip(Dimens.ShapePill)
            .background(BrandGradient)
    } else {
        Modifier.glassSurface(state = hazeState, shape = Dimens.ShapePill)
    }

    Row(
        modifier = background
            .heightIn(min = Dimens.MinTouchTarget)
            .selectable(selected = isSelected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AppColors.OnBackground else AppColors.OnBackgroundMuted,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) AppColors.OnBackground else AppColors.OnBackgroundMuted,
            // Sin esto la etiqueta se parte en dos líneas cuando el ancho aprieta y la pill
            // deja de ser una pill.
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060610, widthDp = 400)
@Composable
private fun FilterTabsPreview() {
    AppTheme {
        FilterTabs(
            selected = PromoTab.ALL,
            counts = TabCounts(all = 24, permanent = 9, limited = 15),
            onTabSelected = {},
            hazeState = rememberHazeState(),
        )
    }
}
