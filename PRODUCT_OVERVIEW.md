# Qué hace la app

Directorio de ofertas de IA y tecnología verificadas para estudiantes universitarios: herramientas, créditos y recursos gratuitos, todo en un solo lugar.

## Flujo de usuario

1. El usuario abre la app y ve una grilla de tarjetas, cada una una oferta.
2. Puede **buscar** por texto libre (busca en el nombre de la empresa y en el título de la oferta).
3. Puede **filtrar** por tab: Todas / Permanentes / Por tiempo limitado.
4. Puede **tocar una tarjeta** para expandirla y leer su descripción completa, que está oculta por defecto. Solo una tarjeta puede estar expandida a la vez en toda la grilla — expandir otra colapsa la anterior.
5. Puede tocar **"Reclamar"** en una tarjeta para abrir el link externo de esa oferta (fuera de la app).
6. Puede tocar la **campana de una tarjeta** para seguir esa oferta y recibir avisos antes de que venza.
7. Puede tocar la **campana del header** para abrir la hoja de avisos y ver todo lo que sigue.

Hay una sola pantalla. Lo único que se le suma es la **hoja de avisos** (`ModalBottomSheet`), que se abre sobre la grilla y se cierra deslizándola: no es una pantalla nueva ni cambia de contexto. No hay navegación a detalle en pantalla completa: expandir es in-place, dentro de la misma tarjeta, en la misma lista.

## Estados de la pantalla principal

- **Cargando:** mientras se pide la lista de ofertas, se muestran placeholders tipo skeleton (misma forma que las tarjetas reales, con un efecto de brillo/shimmer).
- **Error:** si falta configuración (endpoint/credenciales) o falla la llamada de red, se muestra un mensaje de error legible y accionable. **La app nunca debe crashear ni quedar en blanco por esto** — ver la lección documentada en `DATA_AND_API.md`.
- **Vacío:** si la búsqueda/filtro activo no devuelve ninguna oferta, se muestra un mensaje indicando que no hay resultados y sugiriendo probar otro término o filtro. Esto es distinto del estado de error.
- **Con datos:** la grilla de tarjetas, ordenada por urgencia (ver `DATA_AND_API.md`).

## Hoja de avisos

Se abre desde la campana del header. Lista las ofertas que el usuario sigue —sobre el catálogo completo, sin que la búsqueda ni el tab activo la filtren—, cada una con su vencimiento y con si ya fue reclamada o no. Desde ahí se puede dejar de seguir cualquiera.

- **Vacío:** si no sigue ninguna oferta, explica que la campana de una tarjeta es lo que las agrega.
- **Sin permiso:** si faltan los permisos de notificación, aparece arriba un aviso con un botón para activarlos. No bloquea nada: la lista se muestra igual.

## Avisos

Son **locales**, calculados en el dispositivo con WorkManager a partir de `expires_at`. No hay servidor que los mande.

- **Recordatorio de reclamo.** Para cada oferta seguida que todavía no se reclamó, se programan dos avisos: a 3 días y a 1 día del vencimiento. Las ofertas permanentes (`expires_at == null`) no generan ninguno. Si al momento de seguirla ya pasó alguno de esos dos momentos, ese aviso no se programa.
- **Se reverifica al disparar.** Entre programar un aviso y mostrarlo pasan días. Antes de notificar se vuelve a consultar el estado real: si la oferta ya fue reclamada, ya venció, se dejó de seguir o desapareció del catálogo, el aviso no se muestra.
- **Dejar de seguir o reclamar cancela el aviso pendiente**, en el acto.
- **Ofertas nuevas.** Cada ~6 horas, y solo con red disponible, se refresca el catálogo en background y se avisa si aparecieron ofertas que no estaban. Es una aproximación, no un tiempo real: el sistema decide cuándo corre ese refresco, así que el aviso puede llegar con retraso.
- **Reinicio.** Después de reiniciar el dispositivo los recordatorios se recalculan contra el estado actual.
- **El permiso se pide en contexto**, la primera vez que el usuario toca la campana de una oferta — nunca al abrir la app. Si lo niega, no se vuelve a insistir en esa sesión: la oferta queda seguida igual y solo no llegan los avisos.

## Qué NO existe hoy (no inventar funcionalidad)

- **Sin autenticación ni cuentas de usuario.** Todo el contenido es público, sin login.
- **Sin push desde un servidor.** No hay FCM ni backend que empuje nada: todos los avisos se calculan y se programan en el dispositivo (ver "Avisos"). Existió además un teaser de captura de email ("No te pierdas ninguna oferta", con input deshabilitado) y fue eliminado del código de la web a pedido del usuario — no debe replicarse en la app móvil.
- **Sin aviso de "oferta nueva" en el momento.** La detección depende del refresco periódico en background, que el sistema puede demorar.
- **Sin paginación ni scroll infinito.** La Edge Function devuelve todas las ofertas en una sola llamada; hoy son pocas decenas.
- **Sin edición ni creación de ofertas desde la app.** El contenido es de solo lectura; se gestiona desde el backend.
- **Sin modo claro.** El diseño actual es exclusivamente oscuro (ver `DESIGN_SYSTEM.md`); no hay toggle de tema.
