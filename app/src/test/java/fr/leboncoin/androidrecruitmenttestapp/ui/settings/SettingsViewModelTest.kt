package fr.leboncoin.androidrecruitmenttestapp.ui.settings

import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.FakeAppLocaleController
import fr.leboncoin.androidrecruitmenttestapp.FakeLanguagePreferencesRepository
import fr.leboncoin.data.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `no explicit choice made yet exposes a null selected language`() = runTest {
        val viewModel = SettingsViewModel(FakeLanguagePreferencesRepository(), FakeAppLocaleController())

        viewModel.uiState.test {
            assertNull(awaitItem().selectedLanguage)
        }
    }

    @Test
    fun `uiState reflects a previously persisted language`() = runTest {
        val repository = FakeLanguagePreferencesRepository(initialLanguageTag = "fr")
        val viewModel = SettingsViewModel(repository, FakeAppLocaleController())

        viewModel.uiState.test {
            assertEquals(AppLanguage.FRENCH, awaitItem().selectedLanguage)
        }
    }

    @Test
    fun `onLanguageSelected persists the choice and applies it immediately`() = runTest {
        val repository = FakeLanguagePreferencesRepository()
        val localeController = FakeAppLocaleController()
        val viewModel = SettingsViewModel(repository, localeController)

        viewModel.onLanguageSelected(AppLanguage.FRENCH)

        assertEquals("fr", repository.selectedLanguageTag.value)
        assertEquals(listOf("fr"), localeController.appliedLanguageTags)
        viewModel.uiState.test {
            assertEquals(AppLanguage.FRENCH, awaitItem().selectedLanguage)
        }
    }
}
