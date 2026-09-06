package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.data.repository.LanguagePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLanguagePreferencesRepository(
    initialLanguageTag: String? = null,
) : LanguagePreferencesRepository {

    private val state = MutableStateFlow(initialLanguageTag)

    override val selectedLanguageTag = state

    override suspend fun setLanguageTag(languageTag: String) {
        state.value = languageTag
    }
}
