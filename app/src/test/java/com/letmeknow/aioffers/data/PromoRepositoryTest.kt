package com.letmeknow.aioffers.data

import com.letmeknow.aioffers.data.remote.PromosApi
import com.letmeknow.aioffers.data.remote.PromosRemoteDataSource
import com.letmeknow.aioffers.data.remote.SupabaseAuthInterceptor
import com.letmeknow.aioffers.fake.FakePrefsDataSource
import com.letmeknow.aioffers.fake.FakePromoLocalDataSource
import com.letmeknow.aioffers.fake.promo
import com.letmeknow.aioffers.feature.promos.ErrorKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Contrato de error de DATA_AND_API.md, extremo a extremo contra un servidor real.
 *
 * Los cuatro casos de fallo tienen que producir su `ErrorKind` exacto y **ninguno** puede
 * lanzar: `refresh()` siempre devuelve un `Result`.
 */
class PromoRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var local: FakePromoLocalDataSource
    private lateinit var prefs: FakePrefsDataSource
    private lateinit var repository: PromoRepository

    private val anonKey = "anon-key-de-prueba"

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json { ignoreUnknownKeys = true }
        val client = OkHttpClient.Builder()
            .addInterceptor(SupabaseAuthInterceptor(anonKey))
            // Timeouts cortos: el caso de timeout tiene que ser un test rápido, no una espera.
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(server.url("/functions/v1/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PromosApi::class.java)

        local = FakePromoLocalDataSource()
        prefs = FakePrefsDataSource()
        repository = DefaultPromoRepository(PromosRemoteDataSource(api, json), local, prefs)
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun Result<Unit>.errorKind(): ErrorKind =
        requireNotNull(exceptionOrNull()) { "Se esperaba un Result fallido" }.toErrorKind()

    // --- 200 OK -------------------------------------------------------------------------

    @Test
    fun `200 OK cachea las ofertas y las emite por el Flow`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(OK_BODY))

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        val cached = repository.promos.first()
        assertEquals(2, cached.size)

        val copilot = cached.first { it.id == 1L }
        assertEquals("GitHub", copilot.company)
        assertEquals("Copilot gratis", copilot.title)
        assertEquals("https://github.com/edu", copilot.reclaimLink)
        assertEquals(Instant.parse("2026-01-10T00:00:00Z"), copilot.createdAt)
        assertEquals(Instant.parse("2027-03-15T00:00:00Z"), copilot.expiresAt)

        // expires_at nulo llega al dominio como oferta permanente.
        assertEquals(null, cached.first { it.id == 2L }.expiresAt)
    }

    @Test
    fun `la llamada manda los headers de Supabase a promos-batch`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(OK_BODY))

        repository.refresh()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/functions/v1/promos-batch", request.path)
        assertEquals("Bearer $anonKey", request.getHeader("Authorization"))
        assertEquals(anonKey, request.getHeader("apikey"))
    }

    // --- Contrato de error --------------------------------------------------------------

    @Test
    fun `success false es MalformedPayload`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"success":false,"data":[],"count":0}"""),
        )

        assertEquals(ErrorKind.MalformedPayload, repository.refresh().errorKind())
    }

    @Test
    fun `data que no es array es MalformedPayload`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"success":true,"data":{"message":"nope"},"count":0}"""),
        )

        assertEquals(ErrorKind.MalformedPayload, repository.refresh().errorKind())
    }

    @Test
    fun `un cuerpo que no es JSON es MalformedPayload`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html>oops</html>"))

        assertEquals(ErrorKind.MalformedPayload, repository.refresh().errorKind())
    }

    @Test
    fun `una fecha ilegible es MalformedPayload`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"success":true,"data":[{"id":1,"company":"X","title":"Y",""" +
                    """"created_at":"ayer","expires_at":null}],"count":1}""",
            ),
        )

        assertEquals(ErrorKind.MalformedPayload, repository.refresh().errorKind())
    }

    @Test
    fun `500 es Http con el codigo`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        assertEquals(ErrorKind.Http(500), repository.refresh().errorKind())
    }

    @Test
    fun `404 es Http con el codigo`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))

        assertEquals(ErrorKind.Http(404), repository.refresh().errorKind())
    }

    @Test
    fun `un timeout es Network`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(OK_BODY)
                .setBodyDelay(5, TimeUnit.SECONDS),
        )

        assertEquals(ErrorKind.Network, repository.refresh().errorKind())
    }

    @Test
    fun `un servidor caido es Network`() = runTest {
        server.close()

        assertEquals(ErrorKind.Network, repository.refresh().errorKind())
    }

    // --- Caché offline ------------------------------------------------------------------

    @Test
    fun `un refresh fallido deja el cache intacto`() = runTest {
        val previo = promo(id = 99, company = "Notion")
        local.replaceAll(listOf(previo))

        server.enqueue(MockResponse().setResponseCode(500))
        val result = repository.refresh()

        assertEquals(ErrorKind.Http(500), result.errorKind())
        assertEquals(listOf(previo), repository.promos.first())
    }

    @Test
    fun `un refresh exitoso reemplaza el catalogo completo`() = runTest {
        local.replaceAll(listOf(promo(id = 99, company = "Notion")))

        server.enqueue(MockResponse().setResponseCode(200).setBody(OK_BODY))
        repository.refresh()

        // La oferta 99 ya no está en el backend, así que tampoco puede quedar en el caché.
        assertEquals(setOf(1L, 2L), repository.promos.first().map { it.id }.toSet())
    }

    // --- Preferencias -------------------------------------------------------------------

    @Test
    fun `seguir y dejar de seguir se refleja en el Flow`() = runTest {
        assertEquals(emptySet<Long>(), repository.followedIds.first())

        repository.setFollowed(7L, followed = true)
        assertEquals(setOf(7L), repository.followedIds.first())

        repository.setFollowed(7L, followed = false)
        assertEquals(emptySet<Long>(), repository.followedIds.first())
    }

    @Test
    fun `marcar como reclamada se refleja en el Flow`() = runTest {
        repository.markClaimed(3L)

        assertEquals(setOf(3L), repository.claimedIds.first())
    }

    private companion object {
        val OK_BODY = """
            {
              "success": true,
              "count": 2,
              "data": [
                {
                  "id": 1,
                  "company": "GitHub",
                  "title": "Copilot gratis",
                  "description": "Para estudiantes verificados.",
                  "reclaim_link": "https://github.com/edu",
                  "created_at": "2026-01-10T00:00:00Z",
                  "start_date": null,
                  "expires_at": "2027-03-15T00:00:00Z"
                },
                {
                  "id": 2,
                  "company": "Figma",
                  "title": "Plan Education",
                  "description": "Archivos ilimitados.",
                  "reclaim_link": "https://figma.com/edu",
                  "created_at": "2026-02-01T00:00:00+00:00",
                  "start_date": null,
                  "expires_at": null
                }
              ]
            }
        """.trimIndent()
    }
}
