package fr.leboncoin.data.repository

import fr.leboncoin.data.model.Album
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first, single-source-of-truth access to albums.
 *
 * [observeAlbums] and [observeAlbum] always read the local cache (Room) and never touch the
 * network directly that's what makes the list/detail screens work offline and survive process
 * death. [refresh] is the only thing that talks to the network; it just keeps the cache warm.
 * A failed [refresh] therefore never blanks the UI: whatever was last cached stays visible.
 */
interface AlbumRepository {

    fun observeAlbums(): Flow<List<Album>>

    fun observeFavoriteAlbums(): Flow<List<Album>>

    fun observeAlbum(id: Int): Flow<Album?>

    suspend fun refresh(): Result<Unit>

    suspend fun setFavorite(id: Int, isFavorite: Boolean)
}
