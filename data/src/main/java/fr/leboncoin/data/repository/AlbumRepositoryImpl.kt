package fr.leboncoin.data.repository

import fr.leboncoin.core.database.AlbumDao
import fr.leboncoin.core.network.api.AlbumApiService
import fr.leboncoin.data.mapper.toDomain
import fr.leboncoin.data.mapper.toEntity
import fr.leboncoin.data.model.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepositoryImpl @Inject constructor(
    private val albumApiService: AlbumApiService,
    private val albumDao: AlbumDao,
) : AlbumRepository {

    override fun observeAlbums(query: String, favoritesOnly: Boolean, albumGroup: Int?): Flow<List<Album>> =
        albumDao.observeAlbums(query, favoritesOnly, albumGroup).map { entities -> entities.map { it.toDomain() } }

    override fun observeAlbumGroups(): Flow<List<Int>> = albumDao.observeAlbumGroups()

    override fun observeAlbum(id: Int): Flow<Album?> =
        albumDao.observeById(id).map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> = runCatching {
        val remoteAlbums = albumApiService.getAlbums()

        // Fix/design note: a plain `remoteAlbums.map { it.toEntity(isFavorite = false) }` here
        // would silently reset every favorite to false on the very next background refresh,
        // because Room's @Upsert replaces the whole row (see AlbumDaoTest.
        // upsertAll_replacesTheWholeRow_includingFavoriteFlag). Reading the current favorite
        // ids first and carrying them forward is what makes "offline persistence" and
        // "favorites persist locally" compose safely instead of fighting each other.
        val favoriteIds = albumDao.getFavoriteIds().toSet()
        val entities = remoteAlbums.map { dto -> dto.toEntity(isFavorite = dto.id in favoriteIds) }

        albumDao.upsertAll(entities)
    }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        albumDao.setFavorite(id, isFavorite)
    }
}
