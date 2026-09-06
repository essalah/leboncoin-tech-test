package fr.leboncoin.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LanguagePreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : LanguagePreferencesRepository {

    override val selectedLanguageTag: Flow<String?> = dataStore.data.map { it[LANGUAGE_TAG_KEY] }

    override suspend fun setLanguageTag(languageTag: String) {
        dataStore.edit { it[LANGUAGE_TAG_KEY] = languageTag }
    }

    private companion object {
        val LANGUAGE_TAG_KEY = stringPreferencesKey("selected_language_tag")
    }
}
