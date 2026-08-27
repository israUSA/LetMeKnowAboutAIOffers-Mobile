package com.letmeknow.studentoffers.feature.alerts

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.letmeknow.studentoffers.notifications.NotificationPermission

/**
 * El permiso `POST_NOTIFICATIONS` visto desde la UI.
 *
 * **Cuándo se pide** es la única decisión interesante, y está codificada en los dos métodos:
 *
 * - [requestOnce] es el pedido *en contexto*: se dispara la primera vez que el usuario marca
 *   una oferta con la campana, porque recién ahí el permiso tiene un motivo que el usuario
 *   puede entender. Nunca al arrancar. Si dice que no, no se vuelve a preguntar en la sesión:
 *   [askedInSession] es lo que lo garantiza.
 * - [request] es el pedido *explícito*, desde el botón "Activar avisos" del bottom sheet. Ese
 *   sí ignora el "una sola vez", porque lo pidió el usuario.
 *
 * Negar el permiso no rompe nada: el seguimiento se guarda igual en DataStore y simplemente no
 * llegan avisos.
 */
@Stable
class NotificationPermissionState internal constructor(
    private val isRequired: Boolean,
    private val granted: State<Boolean>,
    private val askedInSession: MutableState<Boolean>,
    private val onRequest: () -> Unit,
) {
    val isGranted: Boolean get() = granted.value

    /** Pedido en contexto, como mucho una vez por sesión. */
    fun requestOnce() {
        if (askedInSession.value) return
        request()
    }

    /** Pedido explícito del usuario. */
    fun request() {
        if (!isRequired || isGranted) return
        askedInSession.value = true
        onRequest()
    }
}

/**
 * Crea el estado del permiso atado a la composición actual.
 *
 * `askedInSession` es `rememberSaveable` y no un simple `remember` para que rotar la pantalla
 * no cuente como sesión nueva: girar el teléfono no debería volver a pedir un permiso que el
 * usuario acaba de negar.
 */
@Composable
fun rememberNotificationPermission(): NotificationPermissionState {
    val askedInSession = rememberSaveable { mutableStateOf(false) }

    // En un preview no hay `ActivityResultRegistry`, y `rememberLauncherForActivityResult`
    // revienta al renderizar. `LocalInspectionMode` es constante durante toda la vida de una
    // composición, así que este retorno temprano no puede desbalancear los `remember`.
    if (LocalInspectionMode.current) {
        return remember {
            NotificationPermissionState(
                isRequired = false,
                granted = mutableStateOf(true),
                askedInSession = askedInSession,
                onRequest = {},
            )
        }
    }

    val context = LocalContext.current
    val granted = remember { mutableStateOf(NotificationPermission.isGranted(context)) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        granted.value = isGranted
    }

    return remember(launcher) {
        NotificationPermissionState(
            isRequired = NotificationPermission.isRequired,
            granted = granted,
            askedInSession = askedInSession,
            onRequest = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        )
    }
}
