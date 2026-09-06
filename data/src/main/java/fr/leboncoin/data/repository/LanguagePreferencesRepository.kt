package fr.leboncoin.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persists the user's explicit language override, if any.
 *
 * `null` means no explicit choice has ever been made: the app should just follow the device's
 * locale (falling back to the default `values/` resources for any language it doesn't ship a
 * translation for). Once the user picks a language in Settings, that tag is persisted here and
 * keeps overriding the device locale until the user changes it again.
 */
interface LanguagePreferencesRepository {

    val selectedLanguageTag: Flow<String?>

    suspend fun setLanguageTag(languageTag: String)
}
