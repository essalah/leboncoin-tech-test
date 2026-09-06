package fr.leboncoin.data.repository

import fr.leboncoin.core.database.AlbumDao
import fr.leboncoin.core.database.AlbumEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for [AlbumDao], good enough to unit test [AlbumRepositoryImpl] without a
 * real SQLite database. The actual generated SQL — including the search/favorites/album-group
 * query — is covered separately by the instrumented `AlbumDaoTest` in :core-database; this fake
 * just mirrors that query's *behavior* in plain Kotlin so repository-level tests don't need a
 * device.
 */
class FakeAlbumDao : AlbumDao {

    private val state = MutableStateFlow<List<AlbumEntity>>(emptyList())

    override fun observeAlbums(query: String, favoritesOnly: Boolean, albumGroup: Int?) =
        state.map { albums ->
            albums
                .filter { !favoritesOnly || it.isFavorite }
                .filter { albumGroup == null || it.albumId == albumGroup }
                .filter {
                    query.isEmpty() ||
                        it.title.contains(query, ignoreCase = true) ||
                        it.albumId.toString().contains(query) ||
                        it.id.toString().contains(query)
                }
                .sortedBy { it.id }
        }

    override fun observeAlbumGroups() = state.map { albums -> albums.map { it.albumId }.distinct().sorted() }

    override fun observeById(id: Int) = state.map { albums -> albums.find { it.id == id } }

    override suspend fun getFavoriteIds(): List<Int> =
        state.value.filter { it.isFavorite }.map { it.id }

    override suspend fun upsertAll(albums: List<AlbumEntity>) {
        val byId = state.value.associateBy { it.id }.toMutableMap()
        albums.forEach { byId[it.id] = it }
        state.value = byId.values.toList()
    }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        state.value = state.value.map { if (it.id == id) it.copy(isFavorite = isFavorite) else it }
    }
}
