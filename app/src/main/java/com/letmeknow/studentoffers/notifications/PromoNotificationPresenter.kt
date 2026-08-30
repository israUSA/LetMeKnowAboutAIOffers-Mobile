package com.letmeknow.studentoffers.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.letmeknow.studentoffers.MainActivity
import com.letmeknow.studentoffers.R
import com.letmeknow.studentoffers.domain.model.Promo

class PromoNotificationPresenter(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

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
        if (!NotificationPermission.isGranted(context)) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()

        manager.notify(id, notification)
    }

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
        const val NEW_PROMOS_ID = 1
        const val OPEN_APP_REQUEST_CODE = 0
    }
}
