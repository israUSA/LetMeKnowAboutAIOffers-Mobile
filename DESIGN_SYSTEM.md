# Sistema visual

Referencia del diseño actual de la web: tema oscuro, estilo "glassmorphism" con gradientes vibrantes. Los valores de color y las clases Tailwind citadas abajo son la especificación exacta de lo que se ve — se citan literalmente para que quien lea entienda con precisión qué se está describiendo, no para copiarlas como código Android.

## Tema y fondo

- Fondo base: `#060610` (casi negro, con tinte azul/violeta). Texto general: `#e2e8f0`.
- Fondo "aurora" detrás de todo el contenido, fijo (no scrollea con la página): tres manchas radiales grandes, con blur muy fuerte (~120px), flotando lentamente:
  - Indigo, `rgba(79, 70, 229, .3)` aprox. (Tailwind `bg-indigo-600/30`)
  - Fucsia, `rgba(192, 38, 211, .25)` aprox. (`bg-fuchsia-600/25`)
  - Cian, `rgba(6, 182, 212, .2)` aprox. (`bg-cyan-500/20`)
  - Animación de flotado: 18 segundos, ease-in-out, loop infinito, con distinto delay cada blob para que no se muevan en sincronía (translate + scale sutil, ej. `translate3d(4%, -6%, 0) scale(1.08)`).
  - Encima de los blobs, un radial-gradient oscuro que los va apagando hacia los bordes de la pantalla (`transparent` en el centro-arriba, `rgba(6,6,16,.85)` hacia afuera).

## Glassmorphism

Superficies traslúcidas por todo el sitio: header, tarjetas, buscador, pills de filtro, y los tres estados de la pantalla (carga/error/vacío) comparten el mismo lenguaje:
- Fondo blanco a muy baja opacidad, entre 4% y 9% (`bg-white/[0.04]` a `bg-white/[0.09]`).
- `backdrop-blur` fuerte (`backdrop-blur-xl`).
- Borde blanco translúcido, entre 10% y 25% de opacidad (`border-white/10` a `border-white/25`).
- Esquinas muy redondeadas (`rounded-2xl`/`rounded-3xl`).

## Tipografía

- **Inter** (pesos 400/500/600/700) — fuente general de todo el texto.
- **Space Grotesk** (pesos 500/600/700) — fuente de display, usada en el wordmark de marca y en títulos destacados (hero, nombre de la oferta en cada tarjeta).
- Ambas cargadas desde Google Fonts.

## Acentos de marca

- **Gradiente de texto** (`.text-gradient`): indigo → fucsia → cian, ~100deg (`linear-gradient(100deg, #a5b4fc 0%, #e879f9 45%, #67e8f9 100%)`), aplicado con `background-clip: text` para que el texto quede transparente y muestre el gradiente. Se usa en el wordmark del header/footer y en parte del título del hero.
- **Gradiente sólido** indigo → fucsia (`from-indigo-500 to-fuchsia-500`), usado en el ícono del logo, el tab activo, y el botón "Reclamar" de cada tarjeta.

## Colores de estado (`STATE_COLORS`)

Cada `ExpirationState` tiene un color coherente en: texto del badge, fondo tenue del badge, borde del badge, gradiente de la barra de progreso, y color del glow al hacer hover sobre la tarjeta.

| Estado | Texto | Barra de progreso (gradiente) | Fondo del badge |
|---|---|---|---|
| `permanent` | esmeralda claro (`emerald-300`) | esmeralda → teal | esmeralda al ~10% |
| `comfortable` | celeste (`sky-300`) | celeste → cian | celeste al ~10% |
| `warning` | ámbar (`amber-300`) | ámbar → naranja | ámbar al ~10% |
| `urgent` | rosa (`rose-300`) | rosa/rojo → rojo | rosa al ~12% |

`urgent` es el único estado donde el badge se reemplaza por un countdown en vivo (días:horas:minutos:segundos) en vez de texto estático.

## Colores por empresa (`COMPANY_COLORS`)

Color de fondo sólido del avatar circular (inicial de la empresa en blanco encima):

| Empresa | Color |
|---|---|
| GitHub / GitHub Education | `#24292e` |
| Google | `#4285f4` |
| JetBrains | `#087cfa` |
| Microsoft Azure | `#0078d4` |
| Figma | `#a259ff` |
| Notion | `#000000` |
| AWS | `#ff9900` |
| (cualquier otra, fallback) | `#6366f1` (indigo) |

## Animaciones

| Nombre | Qué hace | Duración | Dónde se usa |
|---|---|---|---|
| `fade-up` | opacity 0→1 + translateY 18px→0 + scale .98→1 | 0.55s, easing suave (cubic-bezier tipo "ease-out-back") | Entrada del hero (con delays escalonados de 60/120/180ms por elemento) y de cada tarjeta de la grilla (delay = índice × 55ms, tope 500ms — efecto de "cascada" al cargar) |
| `float-blob` | translate + scale sutil, loop | 18s, ease-in-out infinito | Los tres blobs del fondo aurora |
| `pulse-slow` | opacity 1→0.6→1 | 3s, ease-in-out infinito | El countdown cuando una oferta es urgente (efecto de "respiración") |
| `shimmer` | barrido de brillo horizontal | 1.6s, ease-in-out infinito | Los placeholders skeleton mientras carga |

**Todas** se desactivan si el sistema tiene activado "reducir movimiento" (`prefers-reduced-motion: reduce`) — replicar ese respeto por la preferencia de accesibilidad del usuario en Android.

## Interacción central: tarjeta expandible

Cada tarjeta de oferta es, en esencia, un botón grande. Al tocarla:
- Se expande verticalmente revelando su descripción completa (antes oculta por completo, no truncada — no se ve ni una preview del texto cuando está colapsada).
- Un ícono de chevron rota 180° para indicar el estado.
- La transición es animada (crecimiento suave de la altura + fade-in del texto), nunca un salto abrupto.
- **Solo una tarjeta puede estar expandida a la vez** en toda la grilla: expandir una colapsa automáticamente la que estuviera abierta antes. El estado de "cuál está expandida" vive en el componente padre de la grilla, no en cada tarjeta individualmente.
- Accesibilidad ya implementada en la web que debe igualarse: el estado expandido/colapsado se anuncia explícitamente (no solo visual), hay foco visible al navegar por teclado/D-pad, y tocar el CTA "Reclamar" dentro de la tarjeta no dispara el expandir/colapsar (son interacciones independientes).

## Layout y responsive

- Contenedor centrado, ancho máximo generoso (equivalente a `max-w-7xl`, ~1280px) en pantallas grandes.
- Grid de tarjetas: 1 columna en pantallas angostas, 2 columnas en medianas, 3 en anchas (en la web: 1 en móvil, 2 en tablet, 3 en desktop). En Android, trasladar como mínimo 1 columna en teléfono; evaluar 2 columnas para tablets/pantallas grandes según el form factor objetivo.
- Área táctil mínima de **44×44** en todo elemento interactivo (tabs de filtro, botón de notificaciones del header, CTA "Reclamar") — mantenerlo como piso mínimo, es un estándar de accesibilidad táctil, no una particularidad de la web.

## Estructura por sección (jerarquía de contenido, no componentes literales)

1. **Header** — fijo arriba de la pantalla. Logo (ícono + wordmark con gradiente) a la izquierda. Botón de "notificaciones" a la derecha (hoy decorativo/deshabilitado, con un pequeño punto indicador encima).
2. **Hero** — badge "N ofertas verificadas" arriba, título grande con parte del texto en gradiente, subtítulo corto, campo de búsqueda con ícono de lupa.
3. **Filtros** — fila de pills/chips (Todas / Permanentes / Por tiempo limitado), cada una con ícono y contador; la pill activa lleva el gradiente de marca de fondo.
4. **Grilla de tarjetas** — el contenido principal. Cada tarjeta, de arriba hacia abajo:
   - Barra de progreso de tiempo (solo si la oferta tiene vencimiento), muy fina, arriba del todo.
   - Fila superior: avatar circular con inicial de empresa + color de marca, nombre de la empresa en mayúsculas pequeñas, título de la oferta en negrita, chevron a la derecha.
   - Panel expandible con la descripción completa (oculto por defecto).
   - Fila inferior: badge de expiración (o countdown si es urgente) a la izquierda, botón "Reclamar" con ícono de enlace externo a la derecha.
5. **Footer** — simple: nombre de marca (con el gradiente de texto) + una línea de disclaimer de que las ofertas pueden cambiar y conviene verificar en el sitio oficial.
