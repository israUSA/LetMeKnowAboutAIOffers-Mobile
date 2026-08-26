# Instrucciones para agentes

App Android nativa (Kotlin + Jetpack Compose) que muestra un directorio de ofertas de IA
verificadas para estudiantes. Trabajás en un worktree, en paralelo con otros dos agentes.

## Regla 1 — Los `.md` son la única fuente de verdad

Antes de escribir código, leé los cuatro documentos de la raíz:

| Documento | Qué contiene |
|---|---|
| `PRODUCT_OVERVIEW.md` | Flujos, estados de pantalla, y **qué NO existe** (no inventar funcionalidad) |
| `DATA_AND_API.md` | Modelo de datos, contrato del endpoint, y las reglas de negocio **exactas** |
| `DESIGN_SYSTEM.md` | Colores, tipografía, animaciones, y la interacción central (tarjeta expandible) |
| `README.md` | Índice |

**Existe un proyecto web de referencia. NO lo abras.** Los `.md` ya destilan todo lo que
hace falta, verificado. Si algo no está en ellos, **preguntá al coordinador** con
`orca orchestration ask`; no lo deduzcas de otro repo ni lo inventes.

## Regla 2 — Archivos que NO podés editar

Estos los mantiene solo el coordinador. Son los únicos que los tres worktrees querrían
tocar a la vez, y sacarlos de la ecuación es lo que hace seguro el paralelismo:

- `gradle/libs.versions.toml` y `gradle/` en general
- `build.gradle.kts` (raíz) y `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/letmeknow/aioffers/ui/theme/**`
- `AGENTS.md`, `CLAUDE.md`, y los cuatro `.md` de especificación

**Si te falta una dependencia, un permiso o un token de tema: escalá, no edites.**
Todas las dependencias que vas a necesitar ya están declaradas en el catálogo de versiones.

## Regla 3 — No hagas commits

No corras `git add`, `git commit`, `git merge` ni `git push`. Dejá tu trabajo en el working
tree. El coordinador revisa el diff, compila y corre los tests; el dueño del repo commitea.

## Regla 4 — Reportá con `worker_done`

```bash
orca orchestration send --type worker_done --subject "<estado>" \
  --body "<qué cambiaste, qué encontraste, qué queda>" \
  --task-id <task_id> --dispatch-id <dispatch_id> \
  --outcome succeeded --files-modified "path/a,path/b" --json
```

Si fallás, usá `--outcome failed`. No codifiques el fallo solo en prosa.

## Arquitectura

- **Un solo módulo `:app`**, capas por paquete. No crear módulos nuevos.
- **MVVM unidireccional.** `PromosViewModel` expone un único `StateFlow<PromosUiState>` y es
  dueño de query, tab, `expandedId` y refresh. Los composables son stateless: reciben
  `(state, onEvent)`.
- **`expandedId` vive en el ViewModel, no en la tarjeta.** `DESIGN_SYSTEM.md` exige que solo
  una tarjeta esté expandida a la vez en toda la grilla; eso solo se garantiza con el estado
  izado a un único dueño.
- **Las reglas de negocio son Kotlin puro**, fuera de Compose, y usan `Clock` inyectable
  (`core/time/Clock.kt`) para ser testeables sin esperar tiempo real.
- **DI manual** en `di/AppContainer.kt`, todo `by lazy`. Sin Hilt.

### El bug que no se puede repetir

`DATA_AND_API.md` documenta que en la web el cliente HTTP se instanciaba a nivel de módulo
sin validar la config. Cuando faltaban URL o key, la excepción se lanzaba **antes de que la
UI montara** y la pantalla quedaba en blanco, sin mensaje ni log.

En Kotlin el equivalente es un `object` con init eager o un `@Provides` no perezoso.
**Nada puede lanzar durante `Application.onCreate`.** `AppConfig.read()` nunca lanza:
devuelve `Valid` o `Missing`, y `Missing` se rutea a
`PromosUiState.Error(ErrorKind.MissingConfig)` con un mensaje que dice qué falta.

### Accesibilidad (no es opcional)

- Área táctil mínima **44×44dp** en todo lo interactivo (`Dimens.MinTouchTarget`).
- El estado expandido/colapsado se anuncia con `stateDescription`, no solo visualmente.
- Foco visible al navegar con teclado o D-pad.
- Tocar "Reclamar" **no** debe expandir/colapsar la tarjeta: son interacciones independientes.
- **Toda** animación consulta `LocalReduceMotion` y se salta si está activo. Usá
  `Motion.durationOrInstant(...)` en vez de las constantes crudas.

## Estructura de paquetes (`com.letmeknow.aioffers`)

```
App.kt                     Application, crea AppContainer
di/AppContainer.kt         DI manual, todo by lazy
core/config/AppConfig.kt   BuildConfig -> Valid | Missing        [ya existe]
core/time/Clock.kt         reloj inyectable                       [ya existe]
core/ui/ReduceMotion.kt    ANIMATOR_DURATION_SCALE                [ya existe]
data/remote/               PromosApi, dto/, interceptor
data/local/                AppDatabase, PromoDao, PrefsDataSource
data/PromoRepository.kt    interfaz                               [ya existe]
domain/model/              Promo, ExpirationState                 [ya existe]
domain/                    ExpirationRules, PromoSorter, PromoFilter
feature/promos/            PromosViewModel, PromosUiState [ya existe], PromosScreen, components/
feature/alerts/            AlertsBottomSheet
notifications/             Notifier [ya existe], workers, scheduler
ui/theme/                  Color, StateColors, Type, Dimens, Motion, Glass, Theme  [NO EDITAR]
```

## Build

Corré Gradle **directo**, desde la raíz de tu worktree:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

**No uses el skill `gradle-run` en este proyecto.** Su `gradle_run.py` exige un "managed
root" dentro del directorio temporal del SO y acá falla con
`managed root must be inside the OS temporary directory`.

El JDK 21 se resuelve solo vía `gradle/gradle-daemon-jvm.properties`. No configures
`JAVA_HOME` ni `org.gradle.java.home` — y si tu shell trae `JAVA_HOME` apuntando a un JDK 11,
hacé `unset JAVA_HOME` antes de correr: AGP 9 necesita 17+.

La primera compilación tarda varios minutos. Si tu herramienta de shell corta antes,
lanzala en background y esperá el resultado; no la des por fallada.

`local.properties` puede estar sin las claves de Supabase. Eso es **correcto**: la app debe
compilar igual y mostrar la pantalla de error de configuración faltante.

## Skills

`chrisbanes/skills` está instalado. El ruteo automático se activa con archivos `.kt`, pero
consultá explícitamente los que apliquen a tu tarea:

- `compose-state-and-effects` — ownership de estado, ciclo de vida de efectos, Flows
- `compose-component-design` — APIs con slots
- `compose-animations` — `fade-up`, `float-blob`, `pulse-slow`, `shimmer`, expand/collapse
- `compose-focus-navigation` — foco de teclado y D-pad
- `compose-performance` — estabilidad, lecturas diferidas, coste de `haze` en scroll
- `compose-ui-testing-patterns` — semantics y screenshot tests
- `kotlin-concurrency-and-flow` — cancelación y modelado de Flows
- `kotlin-control-flow` — `when` exhaustivo sobre tipos sellados
- `kotlin-api-design` — tipos de dominio y fronteras

(`gradle-run` está instalado pero **no funciona en este proyecto** — ver la sección Build.)
