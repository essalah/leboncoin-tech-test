package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.components.image.Illustration
import com.adevinta.spark.components.scaffold.Scaffold
import com.adevinta.spark.components.snackbars.SnackbarHost
import com.adevinta.spark.components.snackbars.SnackbarHostState
import com.adevinta.spark.components.text.Text
import com.adevinta.spark.components.textfields.TextField
import fr.leboncoin.androidrecruitmenttestapp.R
import fr.leboncoin.data.model.Album

private const val EMPTY_STATE_ILLUSTRATION_ALPHA = 0.6f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel,
    onAlbumSelected: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Fix: replaces `LaunchedEffect(Unit) { viewModel.loadAlbums() }`. Loading is now kicked
    // off once from the ViewModel's `init` block instead of from the Composable, so it isn't
    // re-triggered every time this screen re-enters composition (e.g. navigating back from the
    // detail screen) — the ViewModel, not the UI, owns "when do we fetch".
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val albumGroups by viewModel.albumGroups.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Spark's lint rules (spark-lints, run as part of :app:lintDebug) flag raw Material
    // Composables that have a direct Spark replacement — Snackbar/SnackbarHost among them —
    // so the error banner goes through Spark's SnackbarHostState instead of a plain
    // `if (error != null) Snackbar(...)`.
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.dismissError()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            SearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
            )
            FavoritesFilterRow(
                showFavoritesOnly = uiState.showFavoritesOnly,
                onShowFavoritesOnlyChange = viewModel::setShowFavoritesOnly,
            )
            AlbumGroupFilterRow(
                availableGroups = albumGroups,
                selectedGroup = uiState.selectedAlbumGroup,
                onGroupSelected = viewModel::setSelectedAlbumGroup,
            )

            when {
                uiState.albums.isEmpty() && uiState.isRefreshing -> LoadingState()
                uiState.albums.isEmpty() -> EmptyState(
                    isFiltering = uiState.searchQuery.isNotBlank() || uiState.selectedAlbumGroup != null,
                    showingFavorites = uiState.showFavoritesOnly,
                )
                else -> AlbumsList(
                    albums = uiState.albums,
                    onAlbumSelected = onAlbumSelected,
                    onFavoriteToggle = viewModel::toggleFavorite,
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    // Spark's own TextField, not Material3's `SearchBar` — same design-system-consistency
    // reason `Text`/`Snackbar` were switched to Spark equivalents earlier.
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = "Search albums...",
        leadingContent = {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
        },
        trailingContent = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        } else {
            null
        },
    )
}

@Composable
private fun AlbumsList(
    albums: List<Album>,
    onAlbumSelected: (Album) -> Unit,
    onFavoriteToggle: (Album) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        items(items = albums, key = { album -> album.id }) { album ->
            AlbumItem(
                album = album,
                onItemSelected = onAlbumSelected,
                onFavoriteToggle = onFavoriteToggle,
            )
        }
    }
}

@Composable
private fun FavoritesFilterRow(showFavoritesOnly: Boolean, onShowFavoritesOnlyChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = !showFavoritesOnly,
            onClick = { onShowFavoritesOnlyChange(false) },
            label = { Text("All") },
        )
        FilterChip(
            selected = showFavoritesOnly,
            onClick = { onShowFavoritesOnlyChange(true) },
            label = { Text("Favorites") },
        )
    }
}

@Composable
private fun AlbumGroupFilterRow(
    availableGroups: List<Int>,
    selectedGroup: Int?,
    onGroupSelected: (Int?) -> Unit,
) {
    if (availableGroups.isEmpty()) return

    // LazyRow, not a plain Row: the real dataset can have on the order of a hundred distinct
    // album groups — a plain Row would measure and lay out every chip whether visible or not.
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedGroup == null,
                onClick = { onGroupSelected(null) },
                label = { Text("All albums") },
            )
        }
        items(items = availableGroups, key = { it }) { group ->
            FilterChip(
                selected = selectedGroup == group,
                onClick = { onGroupSelected(group) },
                label = { Text("Album $group") },
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(isFiltering: Boolean, showingFavorites: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Reuses the illustration that used to sit behind DetailsActivity's "work in
            // progress" placeholder, instead of leaving it an orphaned, unused drawable now
            // that the detail screen has real content.
            Illustration(
                modifier = Modifier.padding(32.dp),
                painter = painterResource(id = R.drawable.work_in_progress),
                contentDescription = null,
                alpha = EMPTY_STATE_ILLUSTRATION_ALPHA,
            )
            Text(
                text = when {
                    // Checked first: an active search/album-group filter with no matches isn't
                    // an offline problem, and showing "you're offline" here would be actively
                    // misleading.
                    isFiltering -> "No albums match your search"
                    showingFavorites -> "You haven't favorited any album yet"
                    else -> "No albums available — pull down to retry once you're back online"
                },
            )
        }
    }
}
