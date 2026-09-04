package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.FakeAlbumRepository
import fr.leboncoin.androidrecruitmenttestapp.FakeAnalyticsHelper
import fr.leboncoin.androidrecruitmenttestapp.navigation.ALBUM_ID_ARG
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

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

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

    private fun viewModel(albumId: Int, repository: FakeAlbumRepository) = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(ALBUM_ID_ARG to albumId)),
        repository = repository,
        analyticsHelper = FakeAnalyticsHelper(),
    )

    @Test
    fun `exposes the album matching the navigation argument`() = runTest {
        val repository = FakeAlbumRepository(initialAlbums = listOf(album(1), album(2)))

        viewModel(albumId = 2, repository).uiState.test {
            val state = awaitItem()
            assertEquals(2, state.album?.id)
            assertTrue(!state.isLoading)
        }
    }

    @Test
    fun `missing navigation argument fails fast instead of silently showing nothing`() {
        val repository = FakeAlbumRepository()

        assertTrue(
            runCatching {
                DetailViewModel(SavedStateHandle(), repository, FakeAnalyticsHelper())
            }.isFailure,
        )
    }

    @Test
    fun `toggleFavorite flips the current album and is reflected reactively`() = runTest {
        val repository = FakeAlbumRepository(initialAlbums = listOf(album(1)))
        val viewModel = viewModel(albumId = 1, repository)

        viewModel.uiState.test { awaitItem() } // let the first, non-favorite value settle
        viewModel.toggleFavorite()

        viewModel.uiState.test {
            assertTrue(awaitItem().album?.isFavorite == true)
        }
    }
}
