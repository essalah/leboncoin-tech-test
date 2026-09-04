package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.data.model.Album
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAlbumRepository(
    initialAlbums: List<Album> = emptyList(),
    private val refreshResult: () -> Result<Unit> = { Result.success(Unit) },
) : AlbumRepository {

    private val state = MutableStateFlow(initialAlbums)
    var refreshCallCount = 0
        private set

    override fun observeAlbums() = state

    override fun observeFavoriteAlbums() = state.map { albums -> albums.filter { it.isFavorite } }

    override fun observeAlbum(id: Int) = state.map { albums -> albums.find { it.id == id } }

    override suspend fun refresh(): Result<Unit> {
        refreshCallCount++
        return refreshResult()
    }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        state.value = state.value.map { if (it.id == id) it.copy(isFavorite = isFavorite) else it }
    }
}
