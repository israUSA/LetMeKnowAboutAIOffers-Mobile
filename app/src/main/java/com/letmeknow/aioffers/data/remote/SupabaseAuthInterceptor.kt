package com.letmeknow.aioffers.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega los dos headers que Supabase exige en toda llamada a una Edge Function:
 * `Authorization: Bearer <anon key>` y `apikey: <anon key>` (DATA_AND_API.md).
 *
 * La anon key llega por constructor y nunca desde `BuildConfig` directamente: este
 * interceptor solo se construye detrás de un `AppConfig.Valid`, que es la garantía de que la
 * clave existe. Ver el comentario de `AppConfig`.
 */
class SupabaseAuthInterceptor(private val anonKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authorized = chain.request().newBuilder()
            .header("Authorization", "Bearer $anonKey")
            .header("apikey", anonKey)
            .build()
        return chain.proceed(authorized)
    }
}
