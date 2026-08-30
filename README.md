# LetMeKnowAboutAIOffers — Mobile (Kotlin)

**[Descargar el APK (última versión)](https://github.com/israUSA/LetMeKnowAboutAIOffers-Mobile/releases/latest)**

Este directorio es un proyecto nuevo y separado: la app móvil en Kotlin equivalente a la web `LetMeKnowAboutAIOffers`.

**Web de referencia:** `C:\Users\Lenovo\Desktop\Claude Projects\LetMeKnowAboutAIOffers - web` (React 19 + TypeScript + Tailwind v4 + Vite, consume Supabase). El estado completo y actual del diseño vive en la rama `push-changes` de ese repo (commit `72fa96c`), que a la fecha de escribir esto está en un Pull Request abierto hacia `main`. Si ya se mergeó, `main` es la fuente de verdad; si no, usa `push-changes`.

## Qué leer para qué

- **[`PRODUCT_OVERVIEW.md`](./PRODUCT_OVERVIEW.md)** — qué hace la app, flujos de usuario, estados de pantalla, qué NO existe hoy (no inventar funcionalidad).
- **[`DATA_AND_API.md`](./DATA_AND_API.md)** — modelo de datos, contrato del endpoint que hay que consumir, y las reglas de negocio exactas (filtrado, orden, cálculo de expiración, countdown).
- **[`DESIGN_SYSTEM.md`](./DESIGN_SYSTEM.md)** — sistema visual completo: colores, tipografía, animaciones, estructura de cada sección, la interacción central (tarjeta expandible).

## Fuera de alcance de estos documentos

Estos `.md` documentan **qué** debe hacer y **cómo debe verse** la app, con hechos verificados directamente en el código de la web. **No** deciden stack (Kotlin nativo vs Kotlin Multiplatform), arquitectura, librerías, ni estructura de módulos — eso se discute y decide por separado entre los agentes de este proyecto. No asumas ninguna decisión de arquitectura a partir de estos documentos.
