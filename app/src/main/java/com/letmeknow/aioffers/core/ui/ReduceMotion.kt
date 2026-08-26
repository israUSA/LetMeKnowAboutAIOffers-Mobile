package com.letmeknow.aioffers.core.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Equivalente Android de `prefers-reduced-motion: reduce`.
 *
 * En Android no existe esa media query: la señal correcta es `ANIMATOR_DURATION_SCALE`,
 * que el usuario pone en cero desde Accesibilidad ("Quitar animaciones") o desde Opciones
 * de desarrollador. DESIGN_SYSTEM.md exige que **todas** las animaciones lo respeten.
 */
fun Context.isReduceMotionEnabled(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

/**
 * Se provee una vez en la raíz de la composición. Cualquier composable que anime debe leer
 * esto y saltar la animación cuando sea `true`.
 *
 * No tiene default silencioso a propósito: si alguien lo lee sin que se haya provisto,
 * revienta en vez de animar de más sin que nadie se entere.
 */
val LocalReduceMotion: ProvidableCompositionLocal<Boolean> = compositionLocalOf {
    error("LocalReduceMotion no fue provisto. Envolvé el contenido en AppTheme.")
}
