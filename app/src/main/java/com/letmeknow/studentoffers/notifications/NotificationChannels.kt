package com.letmeknow.studentoffers.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.letmeknow.studentoffers.R

/**
 * Los dos canales de la app.
 *
 * Se crean al arrancar, de forma idempotente: `createNotificationChannel` con un id que ya
 * existe actualiza el nombre y la descripción, y no vuelve a pisar la importancia ni el
 * silenciado que el usuario haya elegido. Por eso se puede llamar en cada `onCreate` sin
 * llevar la cuenta de si ya se hizo.
 *
 * `minSdk` es 26, así que los canales existen siempre y no hace falta un chequeo de versión.
 *
 * Crear canales no toca red, ni disco de la app, ni configuración: se puede llamar desde
 * `Application.onCreate` sin violar la regla de "nada puede lanzar al arrancar".
 */
object NotificationChannels {

    /** Recordatorios de "seguís sin reclamar esta oferta y está por vencer". */
    const val CLAIM_REMINDERS = "claim_reminders"

    /** Ofertas nuevas detectadas al refrescar el catálogo en background. */
    const val NEW_PROMOS = "new_promos"

    fun ensureCreated(context: Context) {
        val manager = NotificationManagerCompat.from(context)

        manager.createNotificationChannel(
            // Importancia alta: es un recordatorio con fecha límite; si llega silencioso el
            // día antes del vencimiento, llega para nada.
            channel(
                context = context,
                id = CLAIM_REMINDERS,
                nameRes = R.string.channel_claim_reminders_name,
                descriptionRes = R.string.channel_claim_reminders_description,
                importance = NotificationManager.IMPORTANCE_HIGH,
            ),
        )

        manager.createNotificationChannel(
            // Importancia por defecto: es informativo, no urgente.
            channel(
                context = context,
                id = NEW_PROMOS,
                nameRes = R.string.channel_new_promos_name,
                descriptionRes = R.string.channel_new_promos_description,
                importance = NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    private fun channel(
        context: Context,
        id: String,
        nameRes: Int,
        descriptionRes: Int,
        importance: Int,
    ) = NotificationChannel(id, context.getString(nameRes), importance).apply {
        description = context.getString(descriptionRes)
    }
}
