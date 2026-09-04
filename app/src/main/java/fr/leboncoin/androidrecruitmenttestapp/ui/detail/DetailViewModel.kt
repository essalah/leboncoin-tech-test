package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.androidrecruitmenttestapp.analytics.AnalyticsHelper
import fr.leboncoin.androidrecruitmenttestapp.navigation.ALBUM_ID_ARG
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reads the album id from [SavedStateHandle] (populated by Navigation-Compose from the
 * `detail/{albumId}` route) rather than receiving the whole `Album` as a navigation argument.
 * Re-querying the repository keeps this screen reactive to the single source of truth: a
 * favorite toggled here or on the list screen is reflected on both immediately, and the
 * argument itself is a plain `Int` that trivially survives process death / configuration
 * changes via `SavedStateHandle`.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AlbumRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val albumId: Int = checkNotNull(savedStateHandle[ALBUM_ID_ARG]) {
        "DetailViewModel requires a non-null '$ALBUM_ID_ARG' navigation argument"
    }

    val uiState: StateFlow<DetailUiState> = repository.observeAlbum(albumId)
        .map { album -> DetailUiState(album = album, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = DetailUiState(),
        )

    init {
        analyticsHelper.trackScreenView(SCREEN_NAME)
    }

    fun toggleFavorite() {
        val album = uiState.value.album ?: return
        viewModelScope.launch { repository.setFavorite(album.id, isFavorite = !album.isFavorite) }
    }

    private companion object {
        const val SCREEN_NAME = "Details"
    }
}
