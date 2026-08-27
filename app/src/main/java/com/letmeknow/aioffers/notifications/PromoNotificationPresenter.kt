package com.letmeknow.aioffers.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.letmeknow.aioffers.MainActivity
import com.letmeknow.aioffers.R
import com.letmeknow.aioffers.domain.model.Promo

/**
 * Lo único que arma y publica notificaciones.
 *
 * Existe para que los workers no repitan canal, ícono, `PendingIntent` y chequeo de permiso;
 * y para que ese chequeo esté en un solo lugar. Sin permiso no se publica nada y no se avisa
 * de nada: el usuario que dijo que no ya expresó su preferencia, y el seguimiento de la oferta
 * se guardó igual.
 */
class PromoNotificationPresenter(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    /** "Todavía no reclamaste esta oferta y vence pronto." */
    fun showClaimReminder(promo: Promo, offset: ClaimReminderOffset) {
        val body = when (offset) {
            ClaimReminderOffset.THREE_DAYS ->
                context.getString(R.string.notification_claim_reminder_three_days, promo.title)

            ClaimReminderOffset.ONE_DAY ->
                context.getString(R.string.notification_claim_reminder_one_day, promo.title)
        }

        notify(
            id = CLAIM_ID_BASE + (promo.id % CLAIM_ID_RANGE).toInt(),
            channelId = NotificationChannels.CLAIM_REMINDERS,
            title = context.getString(R.string.notification_claim_reminder_title, promo.company),
            body = body,
            priority = NotificationCompat.PRIORITY_HIGH,
        )
    }

    /** "Aparecieron ofertas que no estaban la última vez que miramos." */
    fun showNewPromos(promos: List<Promo>) {
        if (promos.isEmpty()) return

        val body = if (promos.size == 1) {
            val promo = promos.single()
            context.getString(R.string.notification_new_promos_single, promo.company, promo.title)
        } else {
            context.resources.getQuantityString(
                R.plurals.notification_new_promos_body,
                promos.size,
                promos.size,
            )
        }

        notify(
            id = NEW_PROMOS_ID,
            channelId = NotificationChannels.NEW_PROMOS,
            title = context.getString(R.string.notification_new_promos_title),
            body = body,
            priority = NotificationCompat.PRIORITY_DEFAULT,
        )
    }

    private fun notify(id: Int, channelId: String, title: String, body: String, priority: Int) {
        // El permiso se pide en contexto desde la UI. Acá solo se comprueba: si falta, el
        // aviso simplemente no sale. Nunca es motivo para fallar el worker.
        if (!NotificationPermission.isGranted(context)) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            // Sin esto el texto largo se corta con puntos suspensivos y el aviso no dice
            // de qué oferta habla.
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()

        manager.notify(id, notification)
    }

    /** Tocar el aviso abre la app; no hay pantalla de detalle a la que navegar. */
    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        return PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        /**
         * Un id de notificación por oferta, para que dos recordatorios distintos no se pisen
         * pero el de 3 días sí sea reemplazado por el de 1 día de la misma oferta.
         */
        const val CLAIM_ID_BASE = 1_000
        const val CLAIM_ID_RANGE = 100_000L

        const val NEW_PROMOS_ID = 1
        const val OPEN_APP_REQUEST_CODE = 0
    }
}
