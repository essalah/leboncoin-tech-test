package fr.leboncoin.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The locally persisted, offline-available representation of an album.
 *
 * This table is the app's single source of truth: the UI never reads the network response
 * directly, it reads this table (see [fr.leboncoin.data.repository.AlbumRepository]). Because
 * [isFavorite] lives on the same row as the cached content, "offline persistence" and
 * "favorites persist locally" fall out of one mechanism instead of two.
 */
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean = false,
)
