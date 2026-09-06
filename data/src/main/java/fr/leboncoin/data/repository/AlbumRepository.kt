package fr.leboncoin.data.repository

import fr.leboncoin.data.model.Album
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first, single-source-of-truth access to albums.
 *
 * [observeAlbums] and [observeAlbum] always read the local cache (Room) and never touch the
 * network directly — that's what makes the list/detail screens work offline and survive process
 * death. [refresh] is the only thing that talks to the network; it just keeps the cache warm.
 * A failed [refresh] therefore never blanks the UI: whatever was last cached stays visible.
 */
interface AlbumRepository {

    /**
     * @param query case-insensitive substring match against the title or the numeric
     * album/track id; empty disables text filtering.
     * @param favoritesOnly restrict to favorited albums.
     * @param albumGroup restrict to one `albumId` group; `null` disables this filter.
     */
    fun observeAlbums(query: String = "", favoritesOnly: Boolean = false, albumGroup: Int? = null): Flow<List<Album>>

    /** Distinct album group ids present in the cache, to populate a group filter's options. */
    fun observeAlbumGroups(): Flow<List<Int>>

    fun observeAlbum(id: Int): Flow<Album?>

    suspend fun refresh(): Result<Unit>

    suspend fun setFavorite(id: Int, isFavorite: Boolean)
}
