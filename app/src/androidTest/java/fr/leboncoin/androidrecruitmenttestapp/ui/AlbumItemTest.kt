package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.albums.AlbumItem
import fr.leboncoin.data.model.Album
import org.junit.Rule
import org.junit.Test

/**
 * Exercises the real Compose tree (not a Robolectric/JVM shadow), so it also catches the kind
 * of layout mistake, a modifier stacking constraints in a way that only shows up
 * once actually measured/laid out.
 */
class AlbumItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val album = Album(
        id = 1,
        albumId = 7,
        title = "A memorable album title",
        imageUrl = "https://example.com/1.png",
        thumbnailUrl = "https://example.com/1-thumb.png",
        isFavorite = false,
    )

    @Test
    fun tappingTheCard_invokesOnItemSelected_withThatAlbum() {
        var selected: Album? = null
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = album, onItemSelected = { selected = it }, onFavoriteToggle = {})
            }
        }

        composeTestRule.onNodeWithText(album.title).assertIsDisplayed()
        composeTestRule.onNodeWithText(album.title).performClick()

        assert(selected == album)
    }

    @Test
    fun tappingTheFavoriteIcon_invokesOnFavoriteToggle_withoutSelectingTheItem() {
        var toggled: Album? = null
        var selected: Album? = null
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(
                    album = album,
                    onItemSelected = { selected = it },
                    onFavoriteToggle = { toggled = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()

        assert(toggled == album)
        assert(selected == null)
    }
}
