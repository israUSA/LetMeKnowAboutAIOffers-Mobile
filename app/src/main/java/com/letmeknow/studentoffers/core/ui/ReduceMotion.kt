package com.letmeknow.studentoffers.core.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

fun Context.isReduceMotionEnabled(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

val LocalReduceMotion: ProvidableCompositionLocal<Boolean> = compositionLocalOf {
    error("LocalReduceMotion no fue provisto. Envuelve el contenido en AppTheme.")
}
