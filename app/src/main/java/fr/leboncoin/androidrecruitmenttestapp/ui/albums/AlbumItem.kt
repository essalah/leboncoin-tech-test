package fr.leboncoin.androidrecruitmenttestapp.ui.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.card.Card
import com.adevinta.spark.components.chips.ChipTinted
import com.adevinta.spark.components.text.Text
import fr.leboncoin.data.model.Album

@OptIn(ExperimentalSparkApi::class)
@Composable
fun AlbumItem(
    album: Album,
    onItemSelected: (Album) -> Unit,
    onFavoriteToggle: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 16.dp),
        onClick = { onItemSelected(album) },
    ) {
        Row {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = album.title,
                // this used to be the `modifier` parameter passed into
                // AlbumItem the same instance already applied to the Card above (with its
                // own fillMaxWidth/height/padding). Reapplying it here stacked those
                // constraints onto the thumbnail too. Layout modifiers belong to one node;
                // this image gets its own fresh `Modifier`.
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = album.title,
                        modifier = Modifier.weight(1f),
                        // was `androidx.compose.material3.Text` styled with a
                        // Spark typography token mixing the two design systems. Spark's own
                        // `Text` composable (same API shape) keeps this on one system.
                        style = SparkTheme.typography.caption,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = { onFavoriteToggle(album) }) {
                        Icon(
                            imageVector = if (album.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (album.isFavorite) {
                                "Remove from favorites"
                            } else {
                                "Add to favorites"
                            },
                            tint = if (album.isFavorite) SparkTheme.colors.accent else SparkTheme.colors.outline,
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChipTinted(text = "Album #${album.albumId}")
                    ChipTinted(text = "Track #${album.id}")
                }
            }
        }
    }
}
