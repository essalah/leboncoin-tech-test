package fr.leboncoin.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    /**
     * One flexible query backing the albums list screen instead of separate `observeAll`/
     * `observeFavorites` methods, so search text, the favorites toggle and the album-group
     * filter can all compose in a single SQL call:
     * - `favoritesOnly`: standard Room "optional boolean filter" pattern.
     * - `albumGroup`: `(:albumGroup IS NULL OR albumId = :albumGroup)` is the standard Room
     *   pattern for an optional filter parameter passing `null` disables it entirely.
     * - `query`: matches the title (substring, case-insensitive) OR the numeric album/track id
     *   (as text, so "51" finds `Album #51` too) an empty query disables this clause.
     */
    @Query(
        """
        SELECT * FROM albums
        WHERE (:favoritesOnly = 0 OR isFavorite = 1)
          AND (:albumGroup IS NULL OR albumId = :albumGroup)
          AND (
            :query = ''
            OR title LIKE '%' || :query || '%' COLLATE NOCASE
            OR CAST(albumId AS TEXT) LIKE '%' || :query || '%'
            OR CAST(id AS TEXT) LIKE '%' || :query || '%'
          )
        ORDER BY id
        """,
    )
    fun observeAlbums(query: String, favoritesOnly: Boolean, albumGroup: Int?): Flow<List<AlbumEntity>>

    /** Distinct `albumId` values actually present in the cache, to populate the album-group filter's chips. */
    @Query("SELECT DISTINCT albumId FROM albums ORDER BY albumId")
    fun observeAlbumGroups(): Flow<List<Int>>

    @Query("SELECT * FROM albums WHERE id = :id")
    fun observeById(id: Int): Flow<AlbumEntity?>

    /**
     * Ids currently marked favorite. Read by the repository before a network refresh so a
     * fresh [upsertAll] can carry the flag forward instead of wiping it back to `false` see
     * the note in [fr.leboncoin.data.repository.AlbumRepositoryImpl.refresh].
     */
    @Query("SELECT id FROM albums WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Int>

    @Upsert
    suspend fun upsertAll(albums: List<AlbumEntity>)

    @Query("UPDATE albums SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)
}
