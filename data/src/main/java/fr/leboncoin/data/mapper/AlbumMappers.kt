package fr.leboncoin.data.mapper

import fr.leboncoin.core.database.AlbumEntity
import fr.leboncoin.core.network.model.AlbumDto
import fr.leboncoin.data.model.Album

/**
 * Maps a freshly-fetched DTO into the row that will be written to Room.
 *
 * [isFavorite] is a required parameter (no default) on purpose: it forces every call site to
 * make an explicit decision about the flag instead of silently defaulting to `false`, which is
 * exactly the mistake that would wipe a user's favorites on the next background refresh.
 */
fun AlbumDto.toEntity(isFavorite: Boolean): AlbumEntity = AlbumEntity(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)

fun AlbumEntity.toDomain(): Album = Album(
    id = id,
    albumId = albumId,
    title = title,
    imageUrl = url,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)
