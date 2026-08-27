package com.letmeknow.studentoffers.data.remote

import com.letmeknow.studentoffers.data.remote.dto.PromosBatchResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Edge Function `promos-batch` de Supabase.
 *
 * La base URL es [com.letmeknow.studentoffers.core.config.AppConfig.Valid.functionsBaseUrl], así
 * que la ruta relativa es solo el nombre de la función. Los headers `Authorization` y
 * `apikey` los agrega [SupabaseAuthInterceptor]; no se declaran acá para que no haya dos
 * lugares donde se pueda olvidar uno.
 *
 * Devuelve `Response<...>` en vez del cuerpo pelado porque el contrato de error necesita el
 * código HTTP exacto (`ErrorKind.Http(code)`), no una excepción genérica.
 */
interface PromosApi {

    @GET("promos-batch")
    suspend fun getPromos(): Response<PromosBatchResponse>
}
