package fr.leboncoin.androidrecruitmenttestapp.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsScreen
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.DetailScreen
import fr.leboncoin.androidrecruitmenttestapp.ui.detail.DetailViewModel

const val ALBUM_ID_ARG = "albumId"
private const val ALBUMS_ROUTE = "albums"
private const val DETAIL_ROUTE = "detail/{$ALBUM_ID_ARG}"

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
            AlbumsScreen(
                viewModel = viewModel,
                onAlbumSelected = { album ->
                    viewModel.onAlbumClicked(album)
                    navController.navigate(detailRoute(album.id))
                },
            )
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
    }
}
