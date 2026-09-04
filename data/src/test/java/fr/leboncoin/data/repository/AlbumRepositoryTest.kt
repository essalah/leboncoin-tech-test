package fr.leboncoin.data.repository

import fr.leboncoin.core.network.model.AlbumDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumRepositoryTest {

    private fun dto(id: Int) = AlbumDto(
        id = id,
        albumId = 1,
        title = "title-$id",
        url = "https://example.com/$id.png",
        thumbnailUrl = "https://example.com/$id-thumb.png",
    )

    @Test
    fun `refresh populates the cache from the network`() = runTest {
        val dao = FakeAlbumDao()
        val repository = AlbumRepositoryImpl(FakeAlbumApiService { listOf(dto(1), dto(2)) }, dao)

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        assertEquals(listOf(1, 2), repository.observeAlbums().first().map { it.id })
    }

    @Test
    fun `refresh failure is reported but does not touch the existing cache`() = runTest {
        // Given a cache already warmed by a previous successful refresh
        val dao = FakeAlbumDao()
        val workingApi = FakeAlbumApiService { listOf(dto(1)) }
        AlbumRepositoryImpl(workingApi, dao).refresh()

        // When a later refresh fails (e.g. offline)
        val failingApi = FakeAlbumApiService { throw java.io.IOException("offline") }
        val repository = AlbumRepositoryImpl(failingApi, dao)
        val result = repository.refresh()

        // Then the failure is surfaced as a Result, and the previously cached album is still
        // there this is the offline-first guarantee the ViewModel relies on to never blank
        // the screen on a failed background refresh.
        assertTrue(result.isFailure)
        assertEquals(listOf(1), repository.observeAlbums().first().map { it.id })
    }

    @Test
    fun `refresh carries a locally-set favorite forward instead of resetting it`() = runTest {
        val dao = FakeAlbumDao()
        val repository = AlbumRepositoryImpl(FakeAlbumApiService { listOf(dto(1)) }, dao)
        repository.refresh()
        repository.setFavorite(id = 1, isFavorite = true)

        // A second refresh re-fetches the same album from the network (a fresh DTO, with no
        // notion of "favorite" at all) the repository must still keep it favorited locally.
        repository.refresh()

        val album = repository.observeAlbum(1).first()
        assertTrue(album?.isFavorite == true)
    }

    @Test
    fun `setFavorite is reflected in observeFavoriteAlbums`() = runTest {
        val dao = FakeAlbumDao()
        val repository = AlbumRepositoryImpl(FakeAlbumApiService { listOf(dto(1), dto(2)) }, dao)
        repository.refresh()

        repository.setFavorite(id = 2, isFavorite = true)

        assertEquals(listOf(2), repository.observeFavoriteAlbums().first().map { it.id })
    }
}
