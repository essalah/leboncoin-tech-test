package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.FakeAlbumRepository
import fr.leboncoin.androidrecruitmenttestapp.FakeAnalyticsHelper
import fr.leboncoin.data.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
 * (`AlbumsViewModel(Logger, repository)`) and read `.value` off a `SharedFlow` — neither exists
 * on the actual API, so it could never have compiled, let alone passed. See bug #4 in
 * ARCHITECTURE.md.
 *
 * `uiState` is a `stateIn(..., SharingStarted.WhileSubscribed(5000), ...)` flow: the upstream
 * `combine(...)` only starts once something subscribes, so every assertion below goes through
 * `uiState.test { awaitItem() }` (Turbine) rather than reading `.value` directly — reading
 * `.value` with no active collector would just return the seed `AlbumsUiState()`, not the real
 * state.
 *
 * The class-level `@Before` sets `Dispatchers.Main` to a plain `UnconfinedTestDispatcher()`,
 * which runs coroutines eagerly but has its own independent virtual clock — fine for every test
 * here except the debounce ones, which construct their own `UnconfinedTestDispatcher(testScheduler)`
 * (sharing `runTest`'s scheduler) so `advanceTimeBy` actually moves the search debounce's timer.
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

    private fun album(
        id: Int,
        albumId: Int = 1,
        title: String = "title-$id",
        isFavorite: Boolean = false,
    ) = Album(
        id = id,
        albumId = albumId,
        title = title,
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

    @Test
    fun `setSearchQuery restricts the list to matching titles, after the debounce settles`() = runTest {
        // This test cares about the debounce's timing, so Main needs to share *this* runTest's
        // virtual clock — a plain UnconfinedTestDispatcher() (as used by the class-level
        // @Before) has its own independent clock that advanceTimeBy below would never reach.
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(album(1, title = "lorem ipsum"), album(2, title = "something else")),
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.setSearchQuery("lorem")
        advanceTimeBy(301)

        viewModel.uiState.test {
            assertEquals(listOf(1), awaitItem().albums.map { it.id })
        }
    }

    @Test
    fun `setSearchQuery also matches the numeric album or track id`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(album(51, albumId = 2, title = "unrelated"), album(2)),
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.setSearchQuery("51")
        advanceTimeBy(301)

        viewModel.uiState.test {
            assertEquals(listOf(51), awaitItem().albums.map { it.id })
        }
    }

    @Test
    fun `the initial, empty search query is not delayed by the debounce`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val repository = FakeAlbumRepository(initialAlbums = listOf(album(1)))
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        // No advanceTimeBy at all: if the first query were debounced like every later one,
        // this would still see an empty list.
        viewModel.uiState.test {
            assertEquals(listOf(1), awaitItem().albums.map { it.id })
        }
    }

    @Test
    fun `setSelectedAlbumGroup restricts the list to that group, and toggles off on repeat`() = runTest {
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(album(1, albumId = 1), album(2, albumId = 2)),
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.setSelectedAlbumGroup(1)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(1), state.albums.map { it.id })
            assertEquals(1, state.selectedAlbumGroup)
        }

        viewModel.setSelectedAlbumGroup(1)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(1, 2), state.albums.map { it.id })
            assertEquals(null, state.selectedAlbumGroup)
        }
    }

    @Test
    fun `search, favorites and album group filters all compose together`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(
                album(1, albumId = 1, title = "lorem ipsum"),
                album(2, albumId = 1, title = "lorem dolor", isFavorite = true),
                album(3, albumId = 2, title = "lorem sit", isFavorite = true),
            ),
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        viewModel.setSearchQuery("lorem")
        viewModel.setShowFavoritesOnly(true)
        viewModel.setSelectedAlbumGroup(1)
        advanceTimeBy(301)

        viewModel.uiState.test {
            assertEquals(listOf(2), awaitItem().albums.map { it.id })
        }
    }

    @Test
    fun `albumGroups exposes the distinct groups from the repository`() = runTest {
        val repository = FakeAlbumRepository(
            initialAlbums = listOf(album(1, albumId = 3), album(2, albumId = 1)),
        )
        val viewModel = AlbumsViewModel(repository, FakeAnalyticsHelper())

        // Same reasoning as `uiState`: `albumGroups` is also a WhileSubscribed StateFlow, so it
        // needs an active collector before `.value` reflects anything but the seed emptyList().
        viewModel.albumGroups.test {
            assertEquals(listOf(1, 3), awaitItem())
        }
    }
}
