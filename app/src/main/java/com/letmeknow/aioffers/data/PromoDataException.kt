package com.letmeknow.aioffers.data

import com.letmeknow.aioffers.feature.promos.ErrorKind

/**
 * Portadora del [ErrorKind] dentro del `Result` fallido que devuelve
 * [PromoRepository.refresh].
 *
 * Existe porque `Result` solo sabe transportar `Throwable`. Nunca se lanza hacia afuera de la
 * capa de datos: se construye, se envuelve en `Result.failure` y el ViewModel la vuelve a
 * abrir con [toErrorKind]. Ese es todo su ciclo de vida.
 */
class PromoDataException(val kind: ErrorKind) : Exception(kind.toString())

/**
 * Clasifica el fallo de un `refresh()`.
 *
 * Cualquier cosa que no venga clasificada desde la capa de datos cae en
 * [ErrorKind.Network]: es el mensaje más accionable de los cuatro y, sobre todo, deja la
 * pantalla en el estado de error en vez de dejarla en blanco, que es la regla de
 * DATA_AND_API.md.
 */
fun Throwable.toErrorKind(): ErrorKind = (this as? PromoDataException)?.kind ?: ErrorKind.Network
