package com.letmeknow.studentoffers.ui.brand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Todos los `d` de Simple Icons comparten este viewport. */
private const val ViewportSize = 24f

/**
 * Convierte el `pathData` de un [CompanyIcon] (la sintaxis mini-language de SVG, la misma que
 * usan los vector drawables de Android) en un [ImageVector] pintado con su color de marca.
 *
 * `addPathNodes` ya sabe leer esa sintaxis: no hace falta un parser de SVG genérico para esto.
 */
@Composable
fun rememberCompanyIconVector(icon: CompanyIcon): ImageVector =
    remember(icon) {
        ImageVector.Builder(
            name = "CompanyIcon",
            defaultWidth = ViewportSize.dp,
            defaultHeight = ViewportSize.dp,
            viewportWidth = ViewportSize,
            viewportHeight = ViewportSize,
        ).addPath(
            pathData = addPathNodes(icon.pathData),
            fill = SolidColor(icon.brandColor),
        ).build()
    }
