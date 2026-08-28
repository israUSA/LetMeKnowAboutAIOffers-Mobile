package com.letmeknow.studentoffers.ui.brand

/** Servicio público de favicons de Google, sin autenticación. */
private const val FaviconBaseUrl = "https://www.google.com/s2/favicons"
private const val FaviconSizePx = 128

/**
 * De dónde sale el logo del avatar de una empresa: la cascada de 3 niveles de
 * improve-features.md.
 */
sealed interface LogoSource {
    /** Nivel 1: ícono vectorial de Simple Icons, empaquetado en el build. */
    data class SimpleIcon(val icon: CompanyIcon) : LogoSource

    /** Nivel 2: favicon del dominio real de la empresa, pedido en vivo. */
    data class Favicon(val url: String) : LogoSource

    /** Nivel 3: sin logo mapeado. El círculo de color + inicial de siempre. */
    data object Fallback : LogoSource
}

/**
 * Resuelve de dónde sale el logo de [company], sin tocar la red ni Compose: es lógica pura,
 * fácil de testear.
 *
 * Prueba primero Simple Icons ([companySimpleIcon]), después el favicon del dominio real
 * ([companyFaviconDomain]); si ninguno mapea la empresa, cae en [LogoSource.Fallback]. Una
 * empresa nueva en el catálogo que no esté en ninguno de los dos mapas cae acá sin romperse.
 */
fun resolveLogoSource(company: String): LogoSource {
    companySimpleIcon(company)?.let { return LogoSource.SimpleIcon(it) }
    companyFaviconDomain(company)?.let { return LogoSource.Favicon(faviconUrl(it)) }
    return LogoSource.Fallback
}

private fun faviconUrl(domain: String): String =
    "$FaviconBaseUrl?domain=$domain&sz=$FaviconSizePx"
