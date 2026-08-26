package com.letmeknow.aioffers.core.time

import java.time.Instant

/**
 * Reloj inyectable.
 *
 * Existe para que las reglas de expiración y el countdown sean testeables sin esperar
 * tiempo real: los umbrales de 7 y 30 días, el corte en cero del countdown y el porcentaje
 * de progreso se verifican fijando `now()`.
 */
fun interface Clock {
    fun now(): Instant

    companion object {
        val System = Clock { Instant.now() }
    }
}
