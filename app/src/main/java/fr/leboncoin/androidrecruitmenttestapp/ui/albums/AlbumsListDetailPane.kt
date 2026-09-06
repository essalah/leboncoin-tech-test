package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adevinta.spark.components.divider.VerticalDivider
import fr.leboncoin.androidrecruitmenttestapp.navigation.ALBUM_ID_ARG
import fr.leboncoin.androidrecruitmenttestapp.navigation.DETAIL_ROUTE
import fr.leboncoin.androidrecruitmenttestapp.navigation.detailRoute
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.DetailEmptyState
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.DetailScreen
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.DetailViewModel

private const val DETAIL_EMPTY_ROUTE = "detail_empty"
private const val LIST_PANE_WEIGHT = 0.4f
private const val DETAIL_PANE_WEIGHT = 0.6f

/**
 * Wide-screen (in practice: landscape, plus wide tablets in portrait) counterpart of the regular
 * list -> push-to-detail navigation flow. Shows the album list and the currently-selected
 * album's detail side by side instead, reusing `AlbumsScreen`/`DetailScreen`/`DetailViewModel`
 * entirely unmodified.
 *
 * `DetailViewModel` requires its album id to arrive via `SavedStateHandle` from a nav-compose
 * route argument, so rather than hand-constructing that, the detail pane hosts its own small
 * inner `NavHost` with two destinations: an empty placeholder, and the same `detail/{albumId}`
 * route the outer graph uses for narrow-width push navigation.
 */
@Composable
fun AlbumsListDetailPane(
    albumsViewModel: AlbumsViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailNavController = rememberNavController()
    var selectedAlbumId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Hardware/gesture back while an album is open should clear the selection (back to the
    // empty placeholder), not fall through to the outer NavHost and leave the Albums screen -
    // in this layout the user never "navigated" anywhere in that sense. Disabled once nothing
    // is selected, so back then behaves normally (exits the screen/app, same as today).
    BackHandler(enabled = selectedAlbumId != null) {
        selectedAlbumId = null
        detailNavController.popBackStack(DETAIL_EMPTY_ROUTE, inclusive = false)
    }

    Row(modifier = modifier.fillMaxSize()) {
        AlbumsScreen(
            viewModel = albumsViewModel,
            selectedAlbumId = selectedAlbumId,
            onAlbumSelected = { album ->
                albumsViewModel.onAlbumClicked(album)
                selectedAlbumId = album.id
                // Always replace, never push: the inner back stack should never hold more than
                // one `detail/{albumId}` entry, so tapping through many albums in a row doesn't
                // leak retained DetailViewModel instances.
                detailNavController.navigate(detailRoute(album.id)) {
                    popUpTo(DETAIL_EMPTY_ROUTE) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onOpenSettings = onOpenSettings,
            modifier = Modifier.weight(LIST_PANE_WEIGHT),
        )
        VerticalDivider(modifier = Modifier.fillMaxHeight())
        NavHost(
            navController = detailNavController,
            startDestination = DETAIL_EMPTY_ROUTE,
            modifier = Modifier.weight(DETAIL_PANE_WEIGHT),
        ) {
            composable(DETAIL_EMPTY_ROUTE) { DetailEmptyState() }
            composable(
                route = DETAIL_ROUTE,
                arguments = listOf(navArgument(ALBUM_ID_ARG) { type = NavType.IntType }),
            ) {
                val detailViewModel: DetailViewModel = hiltViewModel()
                DetailScreen(
                    viewModel = detailViewModel,
                    onBack = {
                        selectedAlbumId = null
                        detailNavController.popBackStack(DETAIL_EMPTY_ROUTE, inclusive = false)
                    },
                )
            }
        }
    }
}
