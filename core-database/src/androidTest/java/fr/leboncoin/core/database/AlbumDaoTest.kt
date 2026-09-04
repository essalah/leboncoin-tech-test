package fr.leboncoin.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs against a real, in-memory SQLite database (not a fake) so it also exercises the actual
 * SQL Room generates from the annotations in [AlbumDao] a pure-Kotlin fake couldn't catch a
 * typo'd `@Query`.
 */
@RunWith(AndroidJUnit4::class)
class AlbumDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: AlbumDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.albumDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun album(id: Int, isFavorite: Boolean = false) = AlbumEntity(
        id = id,
        albumId = 1,
        title = "title-$id",
        url = "https://example.com/$id.png",
        thumbnailUrl = "https://example.com/$id-thumb.png",
        isFavorite = isFavorite,
    )

    @Test
    fun upsertAll_thenObserveAll_returnsInsertedRowsOrderedById() = runTest {
        dao.upsertAll(listOf(album(2), album(1), album(3)))

        val result = dao.observeAll().first()

        assertEquals(listOf(1, 2, 3), result.map { it.id })
    }

    @Test
    fun upsertAll_onExistingId_replacesContentColumns() = runTest {
        dao.upsertAll(listOf(album(1)))

        dao.upsertAll(listOf(album(1).copy(title = "updated title")))

        val result = dao.observeById(1).first()
        assertEquals("updated title", result?.title)
    }

    @Test
    fun setFavorite_persistsFlag_andIsReflectedInObserveFavorites() = runTest {
        dao.upsertAll(listOf(album(1), album(2)))

        dao.setFavorite(id = 1, isFavorite = true)

        val favorites = dao.observeFavorites().first()
        assertEquals(listOf(1), favorites.map { it.id })
        assertTrue(dao.getFavoriteIds().contains(1))
    }

    @Test
    fun upsertAll_replacesTheWholeRow_includingFavoriteFlag() = runTest {
        // @Upsert replaces the entire row on conflict it does NOT merge column by column.
        // This test documents that contract on purpose: it's exactly why
        // AlbumRepositoryImpl.refresh() must read getFavoriteIds() and stamp isFavorite back
        // onto each freshly-mapped entity *before* calling upsertAll, instead of assuming the
        // DAO will somehow preserve a locally-set favorite through a network re-sync.
        dao.upsertAll(listOf(album(1, isFavorite = true)))
        assertTrue(dao.getFavoriteIds().contains(1))

        dao.upsertAll(listOf(album(1, isFavorite = false)))

        val result = dao.observeById(1).first()
        assertTrue(result?.isFavorite == false)
    }
}
