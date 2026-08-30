package com.letmeknow.studentoffers.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.letmeknow.studentoffers.R

object NotificationChannels {

    const val NEW_PROMOS = "new_promos"

    fun ensureCreated(context: Context) {
        val manager = NotificationManagerCompat.from(context)

        manager.createNotificationChannel(
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
