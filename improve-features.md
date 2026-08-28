# Feature: logo real por empresa

Fecha: 2026-08-27.

## Qué cambió

Antes, el avatar de cada tarjeta de oferta mostraba **solo un círculo de color sólido con la inicial de la empresa** (dato en `COMPANY_COLORS`, ~7 empresas con color propio, el resto caía en un indigo genérico). Ahora cada tarjeta muestra, cuando es posible, el **logo real de la empresa a color**, con ese círculo de inicial como último recurso.

## Por qué importa para la app móvil

El catálogo real tiene **~41 empresas distintas** (verificado contra la Edge Function `promos-batch` en este turno), no las 7 que estaban hardcodeadas. Cualquier app que muestre este catálogo — incluida la móvil — se enfrenta al mismo problema: la mayoría de las empresas no tenían ninguna identidad visual propia, solo una letra.

## Cómo se resolvió (estrategia en cascada de 3 niveles)

1. **Simple Icons** (paquete de íconos de marca open source, SVG vectorial, a color) — cubre **31 de las 41 empresas**. Zero dependencia de red, empaquetado en el build.
2. **Favicon del dominio real de la empresa** (`https://www.google.com/s2/favicons?domain=<dominio>&sz=128`, servicio público sin autenticación) — cubre **9 empresas más** que Simple Icons no tiene: Microsoft, Microsoft Azure, Amazon, AWS, Amazon Web Services, Adobe, Oracle, IBM y Runway. Con manejo de fallo en runtime (si la imagen no carga, degrada al nivel 3).
3. **Fallback original**: círculo de color sólido + inicial — exactamente el comportamiento de antes, ahora como último recurso. Cualquier empresa nueva que se agregue al catálogo sin logo mapeado cae aquí automáticamente, sin errores.

Resultado: **40 de 41 empresas con logo real** (el 41 pendiente sería solo el caso de una empresa completamente nueva sin mapear).

### Por qué esas 9 empresas concretas no están en Simple Icons

No es un vacío de cobertura al azar: **sus equipos legales exigieron formalmente su remoción** del paquete (verificado en los issues del propio repo `simple-icons/simple-icons`: *"Removal: All Microsoft Icons"* #11236, *"Removal: All IBM icons"* #11258, *"Removal: Oracle"* #11441, *"Remove Amazon / AWS icons"* #13056). Microsoft, IBM y Oracle tienen políticas de trademark que prohíben explícitamente la redistribución de sus logos como paquete reutilizable de terceros — que es exactamente lo que hace Simple Icons.

**Nota para la app móvil:** por esta misma razón, si van a bundlear/empaquetar assets de logos de marca (por ejemplo con Simple Icons para Android, o cualquier librería equivalente), **no intenten conseguir por otra vía los logos de Microsoft/IBM/Oracle/Amazon/AWS/Adobe/Runway** como asset estático — el mismo riesgo de trademark aplica igual en Kotlin que en la web. El mecanismo de favicon (pedirle en vivo a un proxy neutral el ícono que la propia empresa sirve en su sitio, sin redistribuir una copia) es la alternativa que se usó aquí precisamente para evitar ese problema; es una categoría de uso distinta a la de empaquetar un logo pack.

## Cómo se ve

El avatar sigue siendo un círculo de 12×12 (mismo tamaño/posición que antes, no rompe el layout de la tarjeta). Cuando hay logo real (Simple Icons o favicon), el fondo del círculo pasa a **blanco/glass** en vez del color sólido de marca — el logo se ve a color sobre esa superficie clara. El fallback de inicial+color mantiene exactamente el estilo anterior (círculo de color sólido, inicial blanca) para las empresas sin logo disponible.

## Mapa de referencia (empresa → fuente del logo)

**Vía Simple Icons** (slug del paquete): Bolt.new (StackBlitz), Apple, Termius, GitKraken, ElevenLabs, Framer, Miro, Wolfram, Codecademy, Airtable, Cloudflare, Vercel, Namecheap, Sentry, Unity, Autodesk, Alibaba Cloud, DataCamp, Grammarly, Datadog, Google Cloud, Google, Perplexity AI, JetBrains, Notion, MongoDB, Linear, Spotify, GitHub, Figma, Windsurf (Codeium).

**Vía favicon del dominio real**: Microsoft (`microsoft.com`), Microsoft Azure (`azure.microsoft.com`), Amazon (`amazon.com`), AWS (`aws.amazon.com`), Amazon Web Services (`aws.amazon.com`), Adobe (`adobe.com`), Oracle (`oracle.com`), IBM (`ibm.com`), Runway (`runwayml.com`).

**Nota importante:** el dominio real de una empresa **no** se puede inferir del campo `reclaim_link` de cada oferta — se verificó que es poco confiable para esto (ej. OpenAI aparece con `reclaim_link` en `chatgpt.com` y en `help.openai.com`, ninguno de los dos es `openai.com`; IBM usa `skillsbuild.org`; Namecheap usa `nc.me`). El dominio para el logo tiene que curarse a mano por empresa, como se hizo acá — no derivarlo automáticamente del link de la oferta.

## Archivos del repo web que se tocaron

- `src/components/PromoCard.tsx` — lógica de la cascada de 3 niveles en el avatar.
- `src/utils/constants.ts` — nuevos mapas `COMPANY_ICONS` (empresa → ícono de Simple Icons) y `COMPANY_DOMAINS` (empresa → dominio real), junto al `COMPANY_COLORS` que ya existía.
- `package.json` / `package-lock.json` — nueva dependencia `simple-icons`.

No se tocó la lógica de datos, filtros, orden ni el resto de componentes.

## Estado del cambio

Commiteado en `change-desing` (commit `914f69f`) y ya empujado a través de `push-changes`. **[PR #4](https://github.com/israUSA/LetMeKnowAboutAIOffers-Web/pull/4)** (`push-changes` → `main`), abierto sin mergear — verificado contra la API de GitHub: `state=open`, `merged=false`. Como en las rondas anteriores, la producción no mostrará los logos hasta que se mergee ese PR.
