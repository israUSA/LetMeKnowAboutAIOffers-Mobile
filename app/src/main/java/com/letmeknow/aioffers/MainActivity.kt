package com.letmeknow.aioffers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.letmeknow.aioffers.ui.theme.AppColors
import com.letmeknow.aioffers.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // El fondo aurora es fijo y ocupa toda la pantalla, así que el contenido va bajo las
        // barras del sistema.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                // TODO(feat/ui-shell): reemplazar por PromosScreen(viewModel).
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(AppColors.Background),
                )
            }
        }
    }
}
