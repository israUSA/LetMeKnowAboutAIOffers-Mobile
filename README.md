<div align="center">

# 🎓 LetMeKnowAboutStudentOffers

**Ofertas tech verificadas para estudiantes — herramientas, créditos y recursos gratuitos, todo en un solo lugar.**

App Android nativa construida con Kotlin y Jetpack Compose.

[![Descargar APK](https://img.shields.io/badge/%F0%9F%93%B2%20Descargar%20APK-%C3%BAltima%20versi%C3%B3n-4F46E5?style=for-the-badge&logo=android&logoColor=white)](https://github.com/israUSA/LetMeKnowAboutAIOffers-Mobile/releases/latest)

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.08-4285F4?logo=jetpackcompose&logoColor=white)

</div>

## Funciones

- **Catálogo con búsqueda y filtros** — grilla de ofertas con búsqueda por texto libre y pestañas: todas / permanentes / por tiempo limitado.
- **Tarjetas expandibles** — la descripción completa se lee dentro de la tarjeta, y un toque en *Reclamar* abre el sitio de la oferta sin salir de la app.
- **Vigencia calculada en el dispositivo** — countdown para las ofertas por tiempo limitado y estados de expiración en tiempo real.
- **Aviso de ofertas nuevas** — un toggle global notifica cuando entran ofertas nuevas al catálogo.
- **Caché local** — el catálogo se guarda en el dispositivo con Room para agilizar la carga.

## Tecnologías

| | |
|---|---|
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose |
| **Arquitectura** | MVVM unidireccional, un solo módulo `:app` |
| **Red** | Retrofit + OkHttp + kotlinx.serialization |
| **Persistencia** | Room |
| **Imágenes** | Coil |
| **Notificaciones** | WorkManager |
| **Backend** | Supabase |

## Compilar

1. Clona el repositorio y ábrelo en Android Studio (JDK 17+; el wrapper de Gradle resuelve el resto).
2. Genera el APK de debug:

   ```bash
   ./gradlew assembleDebug
   ```

3. Corre los tests:

   ```bash
   ./gradlew testDebugUnitTest
   ```

4. Opcional: define el endpoint y las credenciales en `local.properties` (ver `local.properties.example`). Sin configuración la app compila y se ejecuta igual: muestra su pantalla de error de configuración faltante.

---

<div align="center">

**[Descargar la última versión del APK](https://github.com/israUSA/LetMeKnowAboutAIOffers-Mobile/releases/latest)**

</div>
