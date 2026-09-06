package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.androidrecruitmenttestapp.analytics.AnalyticsHelper
import fr.leboncoin.data.model.Album
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: AlbumRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedAlbumGroup = MutableStateFlow<Int?>(null)
    private val showFavoritesOnly = MutableStateFlow(false)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    // Debounced so typing doesn't fire a Room query per keystroke, but the *initial* value must
    // not be delayed: plain `debounce` would otherwise hold back even the first, empty query for
    // SEARCH_DEBOUNCE_MS before the unfiltered list ever appears. `onStart` re-emits the current
    // value immediately, ahead of collecting the debounced tail; `drop(1)` skips that same
    // value's duplicate replay from `searchQuery` itself so it isn't debounced a second time.
    // This feeds `albums` only `uiState.searchQuery` (what the TextField displays) is sourced
    // from the raw `searchQuery` below instead, so the field reflects every keystroke immediately
    // and doesn't fight the user's cursor while the debounce window is still pending.
    private val debouncedQuery = searchQuery
        .drop(1)
        .debounce(SEARCH_DEBOUNCE_MS)
        .onStart { emit(searchQuery.value) }

    private data class Filters(val query: String, val favoritesOnly: Boolean, val albumGroup: Int?)

    private val filters = combine(debouncedQuery, showFavoritesOnly, selectedAlbumGroup, ::Filters)

    private val albums = filters.flatMapLatest { (query, favoritesOnly, albumGroup) ->
        repository.observeAlbums(query = query, favoritesOnly = favoritesOnly, albumGroup = albumGroup)
    }

    /**
     * The distinct album group ids available to filter by. Its own `StateFlow` rather than a
     * field on [AlbumsUiState]: it's reference data for the filter chips (which groups currently
     * exist in the cache), not per-refresh screen state, and changes independently of and far
     * less often than everything [uiState] tracks.
     */
    val albumGroups: StateFlow<List<Int>> = repository.observeAlbumGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), emptyList())

    // Fix bug the original ViewModel launched its network fetch with
    // `GlobalScope.launch { ... }` (under `@OptIn(DelicateCoroutinesApi::class)`), so the
    // coroutine outlived the ViewModel never cancelled by `onCleared()`, still holding a
    // reference to the repository after the screen was gone. `viewModelScope` (used throughout
    // this class) ties every coroutine to the ViewModel's lifecycle instead.
    //
    // Fix bug the original state was a `MutableSharedFlow` with no replay, so a
    // collector that (re)subscribed after the single `emit()` had already happened e.g. right
    // after a configuration change would simply never receive a value and the screen would
    // stay blank. `stateIn` always has a current value for new collectors, which is exactly the
    // "must survive configuration changes" requirement in the assignment.
    val uiState: StateFlow<AlbumsUiState> = combine(
        albums,
        filters,
        isRefreshing,
        errorMessage,
        searchQuery,
    ) { albums, filters, refreshing, error, query ->
        AlbumsUiState(
            albums = albums,
            showFavoritesOnly = filters.favoritesOnly,
            searchQuery = query,
            selectedAlbumGroup = filters.albumGroup,
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
            // Fix bug the original code was `catch (_: Exception) { /* TODO */ }` 
            // failures were silently dropped, with no loading/error state exposed to the UI at
            // all. A failed refresh is now surfaced as a message, without ever clearing
            // `albums`: whatever was last cached stays on screen.
            repository.refresh()
                .onFailure { errorMessage.value = it.message ?: "Couldn't refresh albums" }
            isRefreshing.value = false
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setShowFavoritesOnly(showFavoritesOnly: Boolean) {
        this.showFavoritesOnly.value = showFavoritesOnly
    }

    /** Tapping the already-selected group's chip again clears the filter back to "all groups". */
    fun setSelectedAlbumGroup(albumGroup: Int?) {
        selectedAlbumGroup.value = if (selectedAlbumGroup.value == albumGroup) null else albumGroup
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
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
