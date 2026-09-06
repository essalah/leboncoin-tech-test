package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.chips.ChipTinted
import com.adevinta.spark.components.scaffold.Scaffold
import com.adevinta.spark.components.text.Text
import fr.leboncoin.androidrecruitmenttestapp.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSparkApi::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.album?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_navigate_back))
                    }
                },
                actions = {
                    uiState.album?.let { album ->
                        IconButton(onClick = viewModel::toggleFavorite) {
                            Icon(
                                imageVector = if (album.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = if (album.isFavorite) {
                                    stringResource(R.string.cd_remove_from_favorites)
                                } else {
                                    stringResource(R.string.cd_add_to_favorites)
                                },
                                tint = if (album.isFavorite) SparkTheme.colors.accent else SparkTheme.colors.outline,
                            )
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        val album = uiState.album
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            // Reachable if the app is opened offline for the very first time (nothing cached
            // yet) and someone deep-links straight into a detail route the id simply isn't
            // in Room yet. Handled explicitly instead of crashing on a null album.
            album == null -> Box(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.detail_album_unavailable_offline)) }

            else -> Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = album.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = album.title, style = SparkTheme.typography.headline2)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipTinted(text = stringResource(R.string.album_number_label, album.albumId))
                        ChipTinted(text = stringResource(R.string.track_number_label, album.id))
                    }
                }
            }
        }
    }
}
