package com.letmeknow.aioffers.core.config

import com.letmeknow.aioffers.BuildConfig

/**
 * Configuración de Supabase, leída de `BuildConfig` (que a su vez viene de `local.properties`,
 * gitignoreado).
 *
 * ESTA CLASE EXISTE POR UN BUG CONCRETO, documentado en DATA_AND_API.md: en la web el cliente
 * HTTP se instanciaba a nivel de módulo sin validar antes que la URL y la key existieran.
 * Cuando faltaban, la excepción se lanzaba **antes de que la UI llegara a montar** y la
 * pantalla quedaba en blanco, sin mensaje y sin log visible.
 *
 * La regla en Kotlin: nadie construye el cliente HTTP sin haber pasado por [read] y obtenido
 * un [Valid]. `AppContainer` lo respeta con inicialización perezosa, y el caso [Missing] se
 * rutea al estado de error de la pantalla principal con un mensaje que dice qué falta.
 */
sealed interface AppConfig {

    data class Valid(val supabaseUrl: String, val anonKey: String) : AppConfig {
        /** URL absoluta de la Edge Function `promos-batch`. */
        val functionsBaseUrl: String get() = "${supabaseUrl.trimEnd('/')}/functions/v1/"
    }

    /** Falta configuración. [missingKeys] son los nombres a mostrar en la pantalla de error. */
    data class Missing(val missingKeys: List<String>) : AppConfig

    companion object {
        const val KEY_URL = "SUPABASE_URL"
        const val KEY_ANON = "SUPABASE_ANON_KEY"

        /**
         * Nunca lanza. Si algo falta devuelve [Missing]; esa es toda la gracia.
         */
        fun read(
            url: String = BuildConfig.SUPABASE_URL,
            anonKey: String = BuildConfig.SUPABASE_ANON_KEY,
        ): AppConfig {
            val missing = buildList {
                if (url.isBlank()) add(KEY_URL)
                if (anonKey.isBlank()) add(KEY_ANON)
            }
            return if (missing.isEmpty()) Valid(url.trim(), anonKey.trim()) else Missing(missing)
        }
    }
}
