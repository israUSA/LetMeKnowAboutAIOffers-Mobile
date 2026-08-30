package com.letmeknow.studentoffers

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
import com.letmeknow.studentoffers.feature.promos.PromosEvent
import com.letmeknow.studentoffers.feature.promos.PromosScreen
import com.letmeknow.studentoffers.feature.promos.PromosViewModel
import com.letmeknow.studentoffers.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PromosViewModel by viewModels {
        (application as App).container.promosViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val notificationsEnabled by viewModel.notificationsEnabled
                    .collectAsStateWithLifecycle()

                PromosScreen(
                    state = state,
                    notificationsEnabled = notificationsEnabled,
                    onEvent = ::handleEvent,
                )
            }
        }
    }

    private fun handleEvent(event: PromosEvent) = when (event) {
        is PromosEvent.OnQueryChange -> viewModel.onQueryChange(event.query)
        is PromosEvent.OnTabSelected -> viewModel.onTabChange(event.tab)
        is PromosEvent.OnCardToggle -> viewModel.onCardClick(event.id)
        is PromosEvent.OnNotificationsToggle -> viewModel.onNotificationsToggle(event.enabled)
        PromosEvent.OnRefresh -> viewModel.onRefresh()
        PromosEvent.OnRetry -> viewModel.onRetry()
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
        }
    }
}
