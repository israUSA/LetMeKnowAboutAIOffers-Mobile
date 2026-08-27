package com.letmeknow.studentoffers.data.remote

import com.letmeknow.studentoffers.data.PromoDataException
import com.letmeknow.studentoffers.data.remote.dto.PromoDto
import com.letmeknow.studentoffers.data.remote.dto.PromosBatchResponse
import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.feature.promos.ErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import java.io.IOException

/**
 * Única puerta de salida a la red, y único lugar donde se decide qué `ErrorKind` corresponde
 * a cada fallo.
 *
 * El contrato de DATA_AND_API.md tiene cuatro casos y los cuatro terminan en la pantalla de
 * error, nunca en un crash:
 *
 * | Qué pasó | `ErrorKind` |
 * |---|---|
 * | Sin conexión, DNS, timeout (todo `IOException`) | [ErrorKind.Network] |
 * | La respuesta no fue 2xx | [ErrorKind.Http] con el código |
 * | 200 pero `success: false` | [ErrorKind.MalformedPayload] |
 * | 200 pero `data` no es un array, o un elemento no parsea | [ErrorKind.MalformedPayload] |
 *
 * `MissingConfig` no aparece acá: si falta configuración esta clase ni se construye. Ver
 * `AppConfig` y `AppContainer`.
 */
class PromosRemoteDataSource(
    private val api: PromosApi,
    private val json: Json,
) {

    /**
     * No lanza salvo cancelación, que sí se propaga: cancelar un refresh no es un error de
     * red y el llamador tiene que verlo como cancelación.
     */
    suspend fun fetchPromos(): Result<List<Promo>> = try {
        val response = api.getPromos()
        when {
            !response.isSuccessful -> failure(ErrorKind.Http(response.code()))
            else -> parseBody(response.body())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        // Sin conexión, DNS que no resuelve, socket caído y timeouts de OkHttp: todos son
        // IOException. No hay rama aparte para timeout porque el contrato los junta.
        failure(ErrorKind.Network)
    } catch (e: Exception) {
        // El conversor de kotlinx.serialization revienta con un cuerpo que ni siquiera es
        // JSON. Es exactamente el caso "payload corrupto".
        failure(ErrorKind.MalformedPayload)
    }

    private fun parseBody(body: PromosBatchResponse?): Result<List<Promo>> {
        val data = body?.data
        return when {
            body == null || !body.success -> failure(ErrorKind.MalformedPayload)
            data !is JsonArray -> failure(ErrorKind.MalformedPayload)
            else -> Result.success(data.map { json.decodeFromJsonElement(PromoDto.serializer(), it).toDomain() })
        }
    }

    private fun failure(kind: ErrorKind): Result<Nothing> = Result.failure(PromoDataException(kind))
}
