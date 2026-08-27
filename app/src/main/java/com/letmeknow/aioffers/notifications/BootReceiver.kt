package com.letmeknow.aioffers.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.letmeknow.aioffers.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reprograma los recordatorios después de un reinicio.
 *
 * WorkManager persiste sus work en disco y los restaura solo, pero los recordatorios se
 * calculan contra `expiresAt`: si el dispositivo estuvo apagado tres días, el aviso "a 3 días
 * del vencimiento" ya no corresponde y el de 1 día sí. `rescheduleAll()` recalcula el plan
 * entero contra el estado actual, que es lo único que deja los avisos consistentes.
 *
 * El trabajo es asíncrono, así que se usa `goAsync()`: sin eso el proceso puede morir apenas
 * `onReceive` retorna, con la corrutina a mitad de camino. El `withTimeoutOrNull` está porque
 * un receiver tiene una ventana de ~10 segundos: si leer Room y DataStore tardara más que eso,
 * conviene rendirse ordenadamente antes de que el sistema mate el proceso.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as? App ?: return
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                withTimeoutOrNull(TIMEOUT_MILLIS) { app.container.notifier.rescheduleAll() }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 8_000L
    }
}
