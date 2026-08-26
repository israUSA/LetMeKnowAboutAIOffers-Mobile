# Datos, API y reglas de negocio

## Modelo de datos — `Promo`

Tal como lo define la web en `src/types/promo.ts`:

```ts
interface Promo {
  id: number
  company: string
  title: string
  description: string
  reclaim_link: string
  created_at: string        // fecha ISO
  start_date: string | null // fecha ISO; declarado pero NO usado hoy en ningún componente de la web
  expires_at: string | null // fecha ISO; null significa oferta permanente, sin vencimiento
}

type ExpirationState = 'permanent' | 'comfortable' | 'warning' | 'urgent'
```

## Endpoint

Supabase Edge Function llamada **`promos-batch`**, método **`GET`**.

En la web se invoca a través del SDK JS: `supabase.functions.invoke('promos-batch', { method: 'GET' })`. En Kotlin esto equivale a un `GET` HTTP directo contra la URL de la función del proyecto de Supabase, enviando:
- Header `Authorization: Bearer <anon key>`
- Header `apikey: <anon key>`

**Forma de la respuesta (200 OK):**
```json
{
  "success": true,
  "data": [ /* array de Promo */ ],
  "count": 8
}
```

**Contrato de error de la app** (cualquiera de estos casos debe tratarse como error, mostrando el estado de error descrito en `PRODUCT_OVERVIEW.md` — nunca un crash):
- La llamada de red falla (sin conexión, timeout, 4xx/5xx).
- `success` viene en `false`.
- `data` no es un array.

## Configuración requerida

Dos valores, con los mismos nombres conceptuales que usa la web (`VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY`):

- **URL del proyecto Supabase** — identificador del proyecto, no es secreto.
- **Anon key** — clave pública anónima del proyecto. Es pública por diseño (termina embebida en cualquier build de cliente), pero de todas formas no debe hardcodearse duplicada en texto plano dentro de este repo de documentación ni copiarse a mano entre proyectos sin necesidad.

Pide ambos valores directamente al usuario/coordinador cuando toque configurarlos, o consulta el archivo `.env` del proyecto web (`C:\Users\Lenovo\Desktop\Claude Projects\LetMeKnowAboutAIOffers - web\.env`, gitignoreado, no versionado). En Android, el equivalente correcto es guardarlos en `local.properties` y exponerlos vía `BuildConfig` (o `secrets.properties` con el plugin de secrets de Gradle) — nunca hardcodeados en el código fuente ni commiteados en texto plano, igual que `.env` está gitignoreado en la web.

## Lección aprendida en la web (aplica igual en Kotlin)

El cliente HTTP de la web se instanciaba a nivel de módulo, sin validar antes que la URL y la key existieran. Cuando faltaban (por ejemplo, un entorno nuevo sin `.env` configurado), la creación del cliente lanzaba una excepción **antes de que la UI llegara a montar**, y la pantalla quedaba completamente en blanco — sin ningún mensaje, sin ningún log visible al usuario.

La corrección: detectar la configuración ausente explícitamente, no dejar que la excepción se propague sin control, y enrutar ese caso al estado de error de la pantalla principal (mensaje claro, "falta configurar X").

**Requisito para la app Kotlin:** valida la configuración (URL/key) antes de construir el cliente HTTP. Si falta algo, la app debe mostrar una pantalla de error legible explicando qué falta — nunca crashear al arrancar ni quedar en una pantalla vacía sin explicación.

## Reglas de negocio (deben replicarse exactamente)

**Búsqueda** — case-insensitive, sobre `company` y `title` únicamente (no busca en `description`).

**Tabs / filtro:**
- `all` → sin filtro adicional, todas las ofertas.
- `permanent` → solo `expires_at == null`.
- `limited` → solo `expires_at != null`.

Los contadores que se muestran junto a cada tab se calculan siempre sobre la lista completa sin filtrar (no sobre el resultado de la búsqueda ni del tab activo).

**Estado de expiración** (`getExpirationState`, umbrales en días):
```
si expires_at == null           → "permanent"
si no:
  days = ceil((expires_at - ahora) / 1 día)
  si days <= 7                  → "urgent"
  si days <= 30                 → "warning"
  si no                         → "comfortable"
```

**Orden de la grilla** — siempre recalculado sobre la lista visible (después de buscar/filtrar), por estado:
```
urgent (0) → warning (1) → comfortable (2) → permanent (3)
```

**Texto de expiración relativa** (`formatRelativeDate`, usado cuando el estado no es `urgent`):
```
days < 0                → "Expirada"
days == 0               → "Expira hoy"
days == 1               → "Expira mañana"
days <= 30               → "Expira en N días"
si no                    → "Expira el D mmm YYYY"   (formato local es-ES, ej: "Expira el 15 mar 2027")
```
Cuando el estado es `permanent`, el texto mostrado es simplemente "Siempre disponible" (no se calcula fecha).

**Progreso de tiempo transcurrido** (`getTimeRemainingPercent`, solo si hay `expires_at`; alimenta la barra de progreso de cada tarjeta):
```
total     = expires_at - created_at
remaining = expires_at - ahora
percent   = clamp(remaining / total * 100, 0, 100)
```

**Countdown en vivo** (solo se muestra cuando el estado es `urgent`, reemplazando el texto normal de expiración): recalcula cada segundo días/horas/minutos/segundos restantes hasta `expires_at`. Si el tiempo ya pasó (diferencia <= 0), muestra todo en cero en vez de números negativos.
