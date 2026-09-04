package fr.leboncoin.data.model

/**
 * Domain model for an album, as the rest of the app (ViewModels, UI) sees it.
 *
 * Deliberately separate from both [fr.leboncoin.core.network.model.AlbumDto] (wire format) and
 * `fr.leboncoin.core.database.AlbumEntity` (storage format): it decouples the UI from either
 * layer's representation, so a JSON field rename or a Room schema migration never has to touch
 * a single Composable.
 */
data class Album(
    val id: Int,
    val albumId: Int,
    val title: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean,
)
