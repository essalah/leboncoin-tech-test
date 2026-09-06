package fr.leboncoin.androidrecruitmenttestapp.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsListDetailPane
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsScreen
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.DetailScreen
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.DetailViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.settings.SettingsScreen
import fr.leboncoin.androidrecruitmenttestapp.ui.settings.SettingsViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.util.rememberIsTwoPane

const val ALBUM_ID_ARG = "albumId"
private const val ALBUMS_ROUTE = "albums"

// Non-private: reused by `AlbumsListDetailPane`'s own inner NavHost so the detail pane can host
// the exact same `detail/{albumId}` destination the outer graph uses for narrow-width push
// navigation.
internal const val DETAIL_ROUTE = "detail/{$ALBUM_ID_ARG}"
private const val SETTINGS_ROUTE = "settings"

fun detailRoute(albumId: Int) = "detail/$albumId"

/**
 * Single navigation graph for the whole app, hosted by the single `MainActivity`. Each
 * destination gets its own `hiltViewModel()` scoped to that back stack entry, so rotating the
 * device mid-navigation keeps both the current screen and the ViewModel state it holds.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ALBUMS_ROUTE) {
        composable(ALBUMS_ROUTE) {
            val viewModel: AlbumsViewModel = hiltViewModel()
            // On wide layouts (in practice: landscape, and wide tablets in portrait) show the
            // list and the selected album's detail side by side instead of pushing a separate
            // screen. `AlbumsListDetailPane` hosts its own inner NavHost for the detail pane, so
            // narrow-width push navigation below is entirely untouched.
            if (rememberIsTwoPane()) {
                AlbumsListDetailPane(
                    albumsViewModel = viewModel,
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
                )
            } else {
                AlbumsScreen(
                    viewModel = viewModel,
                    onAlbumSelected = { album ->
                        viewModel.onAlbumClicked(album)
                        navController.navigate(detailRoute(album.id))
                    },
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
                )
            }
        }
        composable(
            route = DETAIL_ROUTE,
            arguments = listOf(navArgument(ALBUM_ID_ARG) { type = NavType.IntType }),
        ) {
            val viewModel: DetailViewModel = hiltViewModel()
            DetailScreen(
                viewModel = viewModel,
                onBack = navController::popBackStack,
            )
        }
        composable(SETTINGS_ROUTE) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = navController::popBackStack,
            )
        }
    }
}
