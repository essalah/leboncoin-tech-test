package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import fr.leboncoin.data.model.Album

/**
 * Deliberately a single flat state rather than a sealed Loading/Content/Error hierarchy: Room
 * always emits a value immediately (an empty list on a brand-new install, the cached list
 * otherwise), so there is no window where the screen has genuinely "nothing" to show only
 * "cached content, possibly stale, possibly with a refresh in flight, possibly with an error
 * from the last refresh attempt". Modelling those three independent axes as one boolean/nullable
 * combo is both simpler and more accurate than forcing them into mutually-exclusive states.
 */
data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val isRefreshing: Boolean = true,
    val showFavoritesOnly: Boolean = false,
    val errorMessage: String? = null,
)
