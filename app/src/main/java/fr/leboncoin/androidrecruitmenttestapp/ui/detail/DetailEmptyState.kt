package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adevinta.spark.components.image.Illustration
import com.adevinta.spark.components.text.Text
import com.adevinta.spark.icons.InfoOutline
import com.adevinta.spark.icons.SparkIcons
import fr.leboncoin.androidrecruitmenttestapp.R

private const val EMPTY_STATE_ILLUSTRATION_ALPHA = 0.6f

/**
 * Placeholder shown in the detail pane of the list-detail split view (see
 * `AlbumsListDetailPane`) before the user has tapped an album, mirroring the visual pattern of
 * `AlbumsScreen`'s own empty state. Distinct from `DetailScreen`'s "album unavailable offline"
 * message - that one covers a real error, this one covers "nothing selected yet".
 */
@Composable
fun DetailEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Illustration(
                modifier = Modifier.padding(32.dp),
                sparkIcon = SparkIcons.InfoOutline,
                contentDescription = null,
                alpha = EMPTY_STATE_ILLUSTRATION_ALPHA,
            )
            Text(text = stringResource(R.string.detail_pane_select_album_prompt))
        }
    }
}
