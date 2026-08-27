package com.letmeknow.studentoffers.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * `POST_NOTIFICATIONS`, del lado de la plataforma.
 *
 * El permiso es obligatorio desde Android 13; abajo de eso está concedido por definición y
 * `checkSelfPermission` no tiene nada que responder. Toda la app pregunta por acá para que esa
 * bifurcación de versión exista en un solo lugar.
 *
 * Quién pide el permiso está deliberadamente **fuera** de este objeto: se pide en contexto, la
 * primera vez que el usuario toca la campana de una oferta, nunca al arrancar
 * (ver `feature/alerts/NotificationPermissionState`).
 */
object NotificationPermission {

    /** En Android 12 y anteriores no hay permiso que pedir. */
    val isRequired: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /**
     * Si se puede notificar ahora mismo.
     *
     * Mira las dos cosas que pueden bloquear un aviso: el permiso en runtime y el interruptor
     * de notificaciones de la app (que el usuario puede apagar desde Ajustes en cualquier
     * versión, sin que exista permiso alguno).
     */
    fun isGranted(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (!isRequired) return true

        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
}
