# LetMeKnowAboutStudentOffers — Android (Kotlin)

App Android nativa (Kotlin + Jetpack Compose): directorio de ofertas tech verificadas para estudiantes — herramientas, créditos y recursos gratuitos, todo en un solo lugar.

**[Descargar el APK (última versión)](https://github.com/israUSA/LetMeKnowAboutAIOffers-Mobile/releases/latest)**

## Funciones

- Grilla de ofertas con búsqueda por texto libre y filtros: todas / permanentes / por tiempo limitado.
- Cada tarjeta se expande para mostrar la descripción completa y enlaza al sitio donde reclamar la oferta.
- Vigencia calculada en el dispositivo, con countdown para las ofertas por tiempo limitado.
- Toggle global de avisos para enterarte cuando aparecen ofertas nuevas en el catálogo.

## Compilar

1. Clona el repositorio y ábrelo en Android Studio (JDK 17+; el wrapper de Gradle resuelve el resto).
2. `./gradlew assembleDebug` genera el APK de debug.
3. `./gradlew testDebugUnitTest` corre los tests.
4. Opcional: define el endpoint y las credenciales en `local.properties` (ver `local.properties.example`). Sin configuración la app compila y se ejecuta igual: muestra su pantalla de error de configuración faltante.
