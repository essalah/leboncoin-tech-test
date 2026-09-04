package fr.leboncoin.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY id")
    fun observeAll(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE isFavorite = 1 ORDER BY id")
    fun observeFavorites(): Flow<List<AlbumEntity>>

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
