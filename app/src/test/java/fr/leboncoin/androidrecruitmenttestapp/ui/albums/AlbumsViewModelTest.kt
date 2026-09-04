package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.FakeAlbumRepository
import fr.leboncoin.androidrecruitmenttestapp.FakeAnalyticsHelper
import fr.leboncoin.data.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Replaces the original AlbumsViewModelTest, which called a constructor
 * (`AlbumsViewModel(Logger, repository)`) and read `.value` off a `SharedFlow` neither exists
 * on the actual API, so it could never have compiled, let alone passed.
 *
 * `uiState` is a `stateIn(..., SharingStarted.WhileSubscribed(5000), ...)` flow: the upstream
 * `combine(...)` only starts once something subscribes, so every assertion below goes through
 * `uiState.test { awaitItem() }` (Turbine) rather than reading `.value` directly reading
 * `.value` with no active collector would just return the seed `AlbumsUiState()`, not the real
 * state. `UnconfinedTestDispatcher` as `Dispatchers.Main` makes `viewModelScope.launch` run
 * eagerly, so by the time a test subscribes, `init { refresh() }` has already fully settled.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun album(id: Int, isFavorite: Boolean = false) = Album(
        id = id,
        albumId = 1,
        title = "title-$id",
        imageUrl = "https://example.com/$id.png",
        thumbnailUrl = "https://example.com/$id-thumb.png",
        isFavorite = isFavorite,
    )

    @Test
    fun `initial state exposes the cached albums and a refresh is triggered exactly once`() = runTest {
        val repository = FakeAlbumRepository(initialAlbums = listOf(album(1)))
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.uiState.test {
            assertEquals(listOf(1), awaitItem().albums.map { it.id })
        }
        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun `a failed refresh keeps the cached albums visible and surfaces an error`() = runTest {
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(album(1)),
            refreshResult = { Result.failure(IOException("offline")) },
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(1), state.albums.map { it.id })
            assertEquals("offline", state.errorMessage)
            assertTrue(!state.isRefreshing)
        }
    }

    @Test
    fun `toggleFavorite delegates to the repository and is reflected in uiState`() = runTest {
        val repository = FakeAlbumRepository(initialAlbums = listOf(album(1)))
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.toggleFavorite(album(1))

        viewModel.uiState.test {
            assertTrue(awaitItem().albums.single().isFavorite)
        }
    }

    @Test
    fun `setShowFavoritesOnly restricts the list to favorites`() = runTest {
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(album(1, isFavorite = true), album(2)),
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.setShowFavoritesOnly(true)

        viewModel.uiState.test {
            assertEquals(listOf(1), awaitItem().albums.map { it.id })
        }
    }

    @Test
    fun `onAlbumClicked forwards the id to analytics`() = runTest {
        val analytics = FakeAnalyticsHelper()
        val viewModel = AlbumsViewModel(FakeAlbumRepository(), analytics)

        viewModel.onAlbumClicked(album(42))

        assertEquals(listOf("42"), analytics.trackedSelections)
    }

    @Test
    fun `dismissError clears the error message without touching the albums`() = runTest {
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(album(1)),
            refreshResult = { Result.failure(IOException("offline")) },
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.dismissError()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(null, state.errorMessage)
            assertEquals(listOf(1), state.albums.map { it.id })
        }
    }
}
