package com.letmeknow.aioffers

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.letmeknow.aioffers.feature.promos.PromosEvent
import com.letmeknow.aioffers.feature.promos.PromosScreen
import com.letmeknow.aioffers.feature.promos.PromosViewModel
import com.letmeknow.aioffers.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PromosViewModel by viewModels {
        (application as App).container.promosViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // El fondo aurora es fijo y ocupa toda la pantalla, así que el contenido va bajo las
        // barras del sistema.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                PromosScreen(state = state, onEvent = ::handleEvent)
            }
        }
    }

    /**
     * Traduce los eventos de la pantalla a llamadas del ViewModel.
     *
     * Solo `OnClaim` hace algo más que delegar: además de registrar el reclamo abre el link
     * externo. El registro va primero a propósito — lo que importa para los recordatorios es
     * que el usuario expresó la intención, no que el dispositivo tenga un navegador.
     */
    private fun handleEvent(event: PromosEvent) = when (event) {
        is PromosEvent.OnQueryChange -> viewModel.onQueryChange(event.query)
        is PromosEvent.OnTabSelected -> viewModel.onTabChange(event.tab)
        is PromosEvent.OnCardToggle -> viewModel.onCardClick(event.id)
        is PromosEvent.OnFollowToggle -> viewModel.onFollowToggle(event.id, event.followed)
        PromosEvent.OnRefresh -> viewModel.onRefresh()
        PromosEvent.OnRetry -> viewModel.onRetry()
        PromosEvent.OnAlertsOpen -> viewModel.onAlertsOpen()
        PromosEvent.OnAlertsDismiss -> viewModel.onAlertsDismiss()
        is PromosEvent.OnClaim -> {
            viewModel.onClaim(event.promo.promo.id)
            openExternal(event.promo.promo.reclaimLink)
        }
    }

    private fun openExternal(url: String) {
        if (url.isBlank()) return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (_: ActivityNotFoundException) {
            // Sin app capaz de abrir el link. No es motivo para tumbar la pantalla.
        }
    }
}
