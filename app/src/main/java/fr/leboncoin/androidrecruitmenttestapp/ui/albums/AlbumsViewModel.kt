package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.androidrecruitmenttestapp.analytics.AnalyticsHelper
import fr.leboncoin.data.model.Album
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: AlbumRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val showFavoritesOnly = MutableStateFlow(false)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val albums = showFavoritesOnly.flatMapLatest { favoritesOnly ->
        if (favoritesOnly) repository.observeFavoriteAlbums() else repository.observeAlbums()
    }

    // original ViewModel launched its network fetch with
    // `GlobalScope.launch { ... }` (under `@OptIn(DelicateCoroutinesApi::class)`), so the
    // coroutine outlived the ViewModel never cancelled by `onCleared()`, still holding a
    // reference to the repository after the screen was gone. `viewModelScope` (used throughout
    // this class) ties every coroutine to the ViewModel's lifecycle instead.
    //
    // the original state was a `MutableSharedFlow` with no replay, so a
    // collector that (re)subscribed after the single `emit()` had already happened e.g. right
    // after a configuration change would simply never receive a value and the screen would
    // stay blank. `stateIn` always has a current value for new collectors, which is exactly the
    // "must survive configuration changes" requirement in the assignment.
    val uiState: StateFlow<AlbumsUiState> = combine(
        albums,
        showFavoritesOnly,
        isRefreshing,
        errorMessage,
    ) { albums, favoritesOnly, refreshing, error ->
        AlbumsUiState(
            albums = albums,
            showFavoritesOnly = favoritesOnly,
            isRefreshing = refreshing,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = AlbumsUiState(),
    )

    init {
        analyticsHelper.trackScreenView(SCREEN_NAME)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            // the original code was `catch (_: Exception) { /* TODO */ }` —
            // failures were silently dropped, with no loading/error state exposed to the UI at
            // all. A failed refresh is now surfaced as a message, without ever clearing
            // `albums`: whatever was last cached stays on screen.
            repository.refresh()
                .onFailure { errorMessage.value = it.message ?: "Couldn't refresh albums" }
            isRefreshing.value = false
        }
    }

    fun setShowFavoritesOnly(showFavoritesOnly: Boolean) {
        this.showFavoritesOnly.value = showFavoritesOnly
    }

    fun toggleFavorite(album: Album) {
        viewModelScope.launch { repository.setFavorite(album.id, isFavorite = !album.isFavorite) }
    }

    fun onAlbumClicked(album: Album) {
        analyticsHelper.trackSelection(album.id.toString())
    }

    fun dismissError() {
        errorMessage.value = null
    }

    private companion object {
        const val SCREEN_NAME = "Albums"
    }
}
