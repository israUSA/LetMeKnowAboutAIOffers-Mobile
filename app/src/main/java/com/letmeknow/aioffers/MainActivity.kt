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
import com.letmeknow.aioffers.feature.promos.MinimalPromosScreen
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
                // TODO(feat/ui-shell): reemplazar por PromosScreen con el diseño completo.
                MinimalPromosScreen(
                    state = state,
                    onRetry = viewModel::onRetry,
                    onCardClick = viewModel::onCardClick,
                    onFollowToggle = viewModel::onFollowToggle,
                    onQueryChange = viewModel::onQueryChange,
                    onClaim = { model ->
                        // Se registra el reclamo aunque no haya navegador: lo que el usuario
                        // expresó es la intención, y de eso dependen los recordatorios.
                        viewModel.onClaim(model.promo.id)
                        openExternal(model.promo.reclaimLink)
                    },
                )
            }
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
