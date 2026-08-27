# Rebranding: LetMeKnowAboutAIOffers → LetMeKnowAboutStudentOffers

Fecha: 2026-08-27.

## Qué cambió

El nombre de marca del producto pasó de **`LetMeKnowAboutAIOffers`** a **`LetMeKnowAboutStudentOffers`** (unido, camelCase — mismo patrón que el nombre anterior, solo se cambió `AI` por `Student`). El dominio de Vercel también se actualizó a `letmeaboutstudentsoffers`.

**El producto no cambió.** Sigue siendo el mismo directorio de ofertas de IA/tech verificadas para estudiantes universitarios descrito en `PRODUCT_OVERVIEW.md` y `DATA_AND_API.md` de esta carpeta — mismo modelo de datos, mismo endpoint, mismas reglas de negocio. El tagline del hero ("Ofertas de IA y Tech para Estudiantes Universitarios") se mantuvo intacto a propósito: fue una decisión explícita del usuario no generalizarlo, pese a que el nombre nuevo ya no menciona "AI".

## Para los agentes de la app móvil

Usa **`LetMeKnowAboutStudentOffers`** como nombre de marca en la app Kotlin. Si vas a replicar visualmente el wordmark (ver `DESIGN_SYSTEM.md`), el patrón de partición sigue siendo el mismo: la primera parte, **"LetMeKnow"**, lleva el gradiente de marca (indigo→fucsia→cian); el resto, **"AboutStudentOffers"**, va en color de texto normal (blanco sobre el fondo oscuro). No es "AboutAIOffers" en ningún lugar de la UI actual.

## Archivos del repo web que se tocaron

Cambio de texto puro, sin tocar lógica ni estilos más allá del wordmark:

- `index.html` — `<title>`
- `src/components/Header.tsx` — el span blanco del wordmark (encabezado principal del sitio)
- `src/components/Footer.tsx` — el span de marca del pie de página
- `README.md` del repo web — mención de marca actualizada por consistencia
- `package.json` / `package-lock.json` — campo `"name"` del paquete (no visible al usuario final)

No se tocó: `src/hooks/`, `src/lib/supabase.ts`, `src/types/`, `src/utils/`, la lógica de `PromoCard`/`PromoGrid`/filtros, ni el tagline del `Hero`.

## Estado del cambio

Aplicado mediante la misma cadena de worktrees de Orca usada en el resto de este proyecto: `change-desing` (sincronizado primero con `origin/main`, que ya tenía el rediseño y los fixes previos) → `push-changes`. El commit del rebranding es `b89a39b` sobre la rama `push-changes`.

**El cambio está en un Pull Request abierto, sin mergear:** [PR #3](https://github.com/israUSA/LetMeKnowAboutAIOffers-Web/pull/3) (`push-changes` → `main`). El dominio de producción (`letmeaboutstudentsoffers`) **no mostrará el nombre nuevo hasta que ese PR se mergee** — Vercel despliega producción desde `main`.
