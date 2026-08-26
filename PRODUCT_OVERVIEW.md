# Qué hace la app

Directorio de ofertas de IA y tecnología verificadas para estudiantes universitarios: herramientas, créditos y recursos gratuitos, todo en un solo lugar.

## Flujo de usuario

1. El usuario abre la app y ve una grilla de tarjetas, cada una una oferta.
2. Puede **buscar** por texto libre (busca en el nombre de la empresa y en el título de la oferta).
3. Puede **filtrar** por tab: Todas / Permanentes / Por tiempo limitado.
4. Puede **tocar una tarjeta** para expandirla y leer su descripción completa, que está oculta por defecto. Solo una tarjeta puede estar expandida a la vez en toda la grilla — expandir otra colapsa la anterior.
5. Puede tocar **"Reclamar"** en una tarjeta para abrir el link externo de esa oferta (fuera de la app).

No hay más pantallas que esta. No hay navegación a detalle en pantalla completa: expandir es in-place, dentro de la misma tarjeta, en la misma lista.

## Estados de la pantalla principal

- **Cargando:** mientras se pide la lista de ofertas, se muestran placeholders tipo skeleton (misma forma que las tarjetas reales, con un efecto de brillo/shimmer).
- **Error:** si falta configuración (endpoint/credenciales) o falla la llamada de red, se muestra un mensaje de error legible y accionable. **La app nunca debe crashear ni quedar en blanco por esto** — ver la lección documentada en `DATA_AND_API.md`.
- **Vacío:** si la búsqueda/filtro activo no devuelve ninguna oferta, se muestra un mensaje indicando que no hay resultados y sugiriendo probar otro término o filtro. Esto es distinto del estado de error.
- **Con datos:** la grilla de tarjetas, ordenada por urgencia (ver `DATA_AND_API.md`).

## Qué NO existe hoy (no inventar funcionalidad)

- **Sin autenticación ni cuentas de usuario.** Todo el contenido es público, sin login.
- **Sin notificaciones push reales.** Existió un teaser de captura de email ("No te pierdas ninguna oferta", con input deshabilitado) y fue eliminado del código de la web a pedido del usuario — no debe replicarse en la app móvil.
- **Sin paginación ni scroll infinito.** La Edge Function devuelve todas las ofertas en una sola llamada; hoy son pocas decenas.
- **Sin edición ni creación de ofertas desde la app.** El contenido es de solo lectura; se gestiona desde el backend.
- **Sin modo claro.** El diseño actual es exclusivamente oscuro (ver `DESIGN_SYSTEM.md`); no hay toggle de tema.
