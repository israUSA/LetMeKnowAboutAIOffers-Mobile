package com.letmeknow.studentoffers.ui.brand

/**
 * Empresa (clave normalizada, ver [companyFaviconDomain]) -> dominio real, para el nivel 2 de
 * la cascada del avatar (favicon vía `https://www.google.com/s2/favicons?domain=<dominio>`).
 *
 * Estas 9 empresas fueron removidas de Simple Icons a pedido de sus equipos legales (ver
 * improve-features.md), así que el favicon en vivo es la alternativa: pedirle a un proxy
 * neutral el ícono que la propia empresa sirve en su sitio, en vez de redistribuir una copia
 * empaquetada. NO agregar estas empresas a [CompanyIcon] ni a ningún otro asset estático.
 *
 * El dominio se cura a mano por empresa: NO se puede inferir del `reclaim_link` de la oferta
 * (ver improve-features.md — OpenAI usa chatgpt.com, IBM usa skillsbuild.org, Namecheap usa
 * nc.me).
 */
private val CompanyDomains: Map<String, String> = mapOf(
    "microsoft" to "microsoft.com",
    "microsoft azure" to "azure.microsoft.com",
    "amazon" to "amazon.com",
    "aws" to "aws.amazon.com",
    "amazon web services" to "aws.amazon.com",
    "adobe" to "adobe.com",
    "oracle" to "oracle.com",
    "ibm" to "ibm.com",
    "runway" to "runwayml.com",
)

/**
 * Dominio real de una empresa para pedir su favicon, o `null` si no está mapeada.
 *
 * Comparación case-insensitive y tolerante a espacios extra, igual que `companyColor` en
 * `ui/theme/StateColors.kt`.
 */
fun companyFaviconDomain(company: String): String? =
    CompanyDomains[company.trim().lowercase()]
