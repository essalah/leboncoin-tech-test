package fr.leboncoin.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LanguagePreferencesRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun repository() = LanguagePreferencesRepositoryImpl(
        PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("settings.preferences_pb") }),
    )

    @Test
    fun `no explicit choice made yet reads back as null`() = runTest {
        assertNull(repository().selectedLanguageTag.first())
    }

    @Test
    fun `setLanguageTag persists the choice and is reflected by selectedLanguageTag`() = runTest {
        val repository = repository()

        repository.setLanguageTag("fr")

        assertEquals("fr", repository.selectedLanguageTag.first())
    }

    @Test
    fun `setLanguageTag overwrites a previous choice`() = runTest {
        val repository = repository()
        repository.setLanguageTag("fr")

        repository.setLanguageTag("en")

        assertEquals("en", repository.selectedLanguageTag.first())
    }
}
