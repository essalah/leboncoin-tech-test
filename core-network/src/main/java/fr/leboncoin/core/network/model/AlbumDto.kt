package fr.leboncoin.core.network.model

import kotlinx.serialization.Serializable

/**
 * Wire format for one item returned by `GET /img/shared/technical-test.json`.
 *
 * This is a network-layer type on purpose: it must mirror the JSON exactly (including the
 * slightly misleading field names inherited from the JSONPlaceholder `/photos` fixture the
 * endpoint reuses) and must never leak into the UI. [fr.leboncoin.data.mapper] maps this into
 * the domain `Album` model, which is what the rest of the app actually depends on.
 */
@Serializable
data class AlbumDto(
    val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
)
