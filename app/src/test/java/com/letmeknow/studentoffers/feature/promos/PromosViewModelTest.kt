package com.letmeknow.studentoffers.feature.promos

import com.letmeknow.studentoffers.core.config.AppConfig
import com.letmeknow.studentoffers.data.PromoDataException
import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.domain.ExpirationRules
import com.letmeknow.studentoffers.domain.model.ExpirationState
import com.letmeknow.studentoffers.domain.model.Promo
import com.letmeknow.studentoffers.fake.FakeClock
import com.letmeknow.studentoffers.fake.FakeNotificationsPreferences
import com.letmeknow.studentoffers.fake.NOW
import com.letmeknow.studentoffers.fake.days
import com.letmeknow.studentoffers.fake.promo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class PromosViewModelTest {

    private val clock = FakeClock(NOW)
    private val rules = ExpirationRules(clock, ZoneOffset.UTC)
    private val dispatcher = StandardTestDispatcher()
    private val notificationsPreferences = FakeNotificationsPreferences()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRepository(
        promos: List<Promo> = emptyList(),
        private val refreshResult: Result<Unit> = Result.success(Unit),
    ) : PromoRepository {
        private val state = MutableStateFlow(promos)
        private val claimed = MutableStateFlow(emptySet<Long>())

        var refreshCount = 0
            private set

        override val promos: Flow<List<Promo>> = state
        override val claimedIds: Flow<Set<Long>> = claimed

        override suspend fun refresh(): Result<Unit> {
            refreshCount++
            return refreshResult
        }

        override suspend fun markClaimed(promoId: Long) {
            claimed.value = claimed.value + promoId
        }
    }

    private fun viewModel(
        repository: FakeRepository,
        config: AppConfig = AppConfig.Valid("https://proyecto.supabase.co", "anon"),
    ) = PromosViewModel(
        config = config,
        rules = rules,
        repositoryProvider = { repository },
        notificationsPreferences = notificationsPreferences,
    )

    private fun TestScope.subscribe(viewModel: PromosViewModel) {
        backgroundScope.launch { viewModel.state.collect() }
        runCurrent()
    }

    private fun PromosViewModel.content(): PromosUiState.Content =
        state.value as PromosUiState.Content

    @Test
    fun `config Missing emite MissingConfig sin construir el repositorio`() {
        var construido = false

        val viewModel = PromosViewModel(
            config = AppConfig.Missing(listOf(AppConfig.KEY_URL, AppConfig.KEY_ANON)),
            rules = rules,
            repositoryProvider = {
                construido = true
                error("El repositorio no puede construirse sin configuración")
            },
            notificationsPreferences = notificationsPreferences,
        )

        assertEquals(
            PromosUiState.Error(
                ErrorKind.MissingConfig(listOf("SUPABASE_URL", "SUPABASE_ANON_KEY")),
            ),
            viewModel.state.value,
        )
        assertFalse(construido)
    }

    @Test
    fun `arranca en Loading`() = runTest(dispatcher) {
        val viewModel = viewModel(FakeRepository())

        assertEquals(PromosUiState.Loading, viewModel.state.value)
    }

    @Test
    fun `sin cache y con refresh fallido muestra la pantalla de error`() = runTest(dispatcher) {
        val repository = FakeRepository(
            refreshResult = Result.failure(PromoDataException(ErrorKind.Http(503))),
        )
        val viewModel = viewModel(repository)

        subscribe(viewModel)

        assertEquals(PromosUiState.Error(ErrorKind.Http(503)), viewModel.state.value)
    }

    @Test
    fun `con cache y refresh fallido muestra los datos marcados como stale`() = runTest(dispatcher) {
        val repository = FakeRepository(
            promos = listOf(promo(id = 1, expiresAt = null)),
            refreshResult = Result.failure(PromoDataException(ErrorKind.Network)),
        )
        val viewModel = viewModel(repository)

        subscribe(viewModel)

        val content = viewModel.content()
        assertEquals(1, content.promos.size)
        assertTrue(content.isStale)
        assertFalse(content.isRefreshing)
    }

    @Test
    fun `un refresh exitoso no marca stale`() = runTest(dispatcher) {
        val repository = FakeRepository(promos = listOf(promo(id = 1, expiresAt = null)))
        val viewModel = viewModel(repository)

        subscribe(viewModel)

        assertFalse(viewModel.content().isStale)
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `un refresh exitoso posterior limpia el estado de error`() = runTest(dispatcher) {
        val repository = FakeRepository(
            promos = listOf(promo(id = 1, expiresAt = null)),
            refreshResult = Result.failure(PromoDataException(ErrorKind.Network)),
        )
        val viewModel = viewModel(repository)
        subscribe(viewModel)
        assertTrue(viewModel.content().isStale)

        val sano = FakeRepository(promos = listOf(promo(id = 1, expiresAt = null)))
        val otro = viewModel(sano)
        subscribe(otro)

        assertFalse(otro.content().isStale)
    }

    @Test
    fun `los contadores se calculan sobre el catalogo completo aunque la busqueda filtre`() =
        runTest(dispatcher) {
            val repository = FakeRepository(
                promos = listOf(
                    promo(id = 1, company = "GitHub", expiresAt = null),
                    promo(id = 2, company = "Figma", expiresAt = NOW.plusSeconds(days(90))),
                    promo(id = 3, company = "Notion", expiresAt = NOW.plusSeconds(days(60))),
                ),
            )
            val viewModel = viewModel(repository)
            subscribe(viewModel)

            val esperado = TabCounts(all = 3, permanent = 1, limited = 2)

            viewModel.onQueryChange("figma")
            runCurrent()
            assertEquals(listOf(2L), viewModel.content().promos.map { it.promo.id })
            assertEquals(esperado, viewModel.content().counts)

            viewModel.onTabChange(PromoTab.PERMANENT)
            runCurrent()
            assertTrue(viewModel.content().isEmpty)
            assertEquals(esperado, viewModel.content().counts)
        }

    @Test
    fun `las ofertas salen ordenadas por urgencia`() = runTest(dispatcher) {
        val repository = FakeRepository(
            promos = listOf(
                promo(id = 1, expiresAt = null),
                promo(id = 2, expiresAt = NOW.plusSeconds(days(90))),
                promo(id = 3, expiresAt = NOW.plusSeconds(days(2))),
                promo(id = 4, expiresAt = NOW.plusSeconds(days(20))),
            ),
        )
        val viewModel = viewModel(repository)
        subscribe(viewModel)

        val content = viewModel.content()
        assertEquals(listOf(3L, 4L, 2L, 1L), content.promos.map { it.promo.id })
        assertEquals(
            listOf(
                ExpirationState.URGENT,
                ExpirationState.WARNING,
                ExpirationState.COMFORTABLE,
                ExpirationState.PERMANENT,
            ),
            content.promos.map { it.state },
        )
    }

    @Test
    fun `solo una tarjeta queda expandida a la vez`() = runTest(dispatcher) {
        val repository = FakeRepository(
            promos = listOf(promo(id = 1, expiresAt = null), promo(id = 2, expiresAt = null)),
        )
        val viewModel = viewModel(repository)
        subscribe(viewModel)

        assertEquals(null, viewModel.content().expandedId)

        viewModel.onCardClick(1L)
        runCurrent()
        assertEquals(1L, viewModel.content().expandedId)

        viewModel.onCardClick(2L)
        runCurrent()
        assertEquals(2L, viewModel.content().expandedId)

        viewModel.onCardClick(2L)
        runCurrent()
        assertEquals(null, viewModel.content().expandedId)
    }

    @Test
    fun `reclamar se refleja en el modelo de la tarjeta`() = runTest(dispatcher) {
        val repository = FakeRepository(promos = listOf(promo(id = 1, expiresAt = null)))
        val viewModel = viewModel(repository)
        subscribe(viewModel)

        assertFalse(viewModel.content().promos.single().isClaimed)

        viewModel.onClaim(1L)
        runCurrent()

        assertTrue(viewModel.content().promos.single().isClaimed)
    }

    @Test
    fun `con una oferta urgente el ticker recalcula cada segundo`() = runTest(dispatcher) {
        val repository = FakeRepository(
            promos = listOf(
                promo(
                    id = 1,
                    createdAt = NOW.minusSeconds(days(10)),
                    expiresAt = NOW.plusSeconds(days(2)),
                ),
            ),
        )
        val viewModel = viewModel(repository)
        subscribe(viewModel)

        val antes = viewModel.content().promos.single().timeRemainingPercent!!

        clock.instant = NOW.plusSeconds(3_600)
        advanceTimeBy(1_100)
        runCurrent()

        val despues = viewModel.content().promos.single().timeRemainingPercent!!
        assertTrue("$despues debería ser menor que $antes", despues < antes)
    }

    @Test
    fun `sin ofertas urgentes el ticker no corre`() = runTest(dispatcher) {
        val repository = FakeRepository(promos = listOf(promo(id = 1, expiresAt = null)))
        val viewModel = viewModel(repository)

        val emitidos = mutableListOf<PromosUiState>()
        backgroundScope.launch { viewModel.state.toList(emitidos) }
        runCurrent()

        val hasta = emitidos.size
        advanceTimeBy(10_000)
        runCurrent()

        assertEquals(hasta, emitidos.size)
    }

    @Test
    fun `los avisos arrancan apagados y la campana los enciende y los apaga`() =
        runTest(dispatcher) {
            val viewModel = viewModel(FakeRepository(promos = listOf(promo(id = 1))))
            backgroundScope.launch { viewModel.notificationsEnabled.collect() }
            runCurrent()

            assertFalse(viewModel.notificationsEnabled.value)

            viewModel.onNotificationsToggle(true)
            runCurrent()
            assertTrue(viewModel.notificationsEnabled.value)
            assertTrue(notificationsPreferences.enabled.first())

            viewModel.onNotificationsToggle(false)
            runCurrent()
            assertFalse(viewModel.notificationsEnabled.value)
            assertFalse(notificationsPreferences.enabled.first())
        }

    @Test
    fun `el toggle funciona con la pantalla en error por configuracion faltante`() =
        runTest(dispatcher) {
            val viewModel = PromosViewModel(
                config = AppConfig.Missing(listOf(AppConfig.KEY_URL)),
                rules = rules,
                repositoryProvider = { error("No hay repositorio sin configuración") },
                notificationsPreferences = notificationsPreferences,
            )
            backgroundScope.launch { viewModel.notificationsEnabled.collect() }
            runCurrent()

            assertTrue(viewModel.state.value is PromosUiState.Error)

            viewModel.onNotificationsToggle(true)
            runCurrent()

            assertTrue(viewModel.notificationsEnabled.value)
        }
}
