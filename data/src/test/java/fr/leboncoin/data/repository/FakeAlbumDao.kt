package fr.leboncoin.data.repository

import fr.leboncoin.core.database.AlbumDao
import fr.leboncoin.core.database.AlbumEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for [AlbumDao], good enough to unit test [AlbumRepositoryImpl] without a
 * real SQLite database. The actual generated SQL is covered separately by the instrumented
 * `AlbumDaoTest` in :core-database.
 */
class FakeAlbumDao : AlbumDao {

    private val state = MutableStateFlow<List<AlbumEntity>>(emptyList())

    override fun observeAll() = state.map { albums -> albums.sortedBy { it.id } }

    override fun observeFavorites() = state.map { albums ->
        albums.filter { it.isFavorite }.sortedBy { it.id }
    }

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
