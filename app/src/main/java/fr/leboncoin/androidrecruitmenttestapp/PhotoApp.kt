package fr.leboncoin.androidrecruitmenttestapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import fr.leboncoin.androidrecruitmenttestapp.locale.AppLocaleController
import fr.leboncoin.data.repository.LanguagePreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * `@HiltAndroidApp` triggers Hilt's code generation and creates the app-level (Singleton)
 * dependency container. This replaces the old hand-rolled `AppDependenciesProvider` service
 * locator dependencies are now requested with `@Inject`/`hiltViewModel()` instead of every
 * Activity casting `application` and pulling from a shared bag of `by lazy` properties.
 */
@HiltAndroidApp
class PhotoApp : Application() {

    @Inject
    lateinit var languagePreferencesRepository: LanguagePreferencesRepository

    @Inject
    lateinit var localeController: AppLocaleController

    override fun onCreate() {
        super.onCreate()

        // Re-apply a previously chosen language override before any UI is built. If the user has
        // never picked one, this stays null and the app just follows the device locale (falling
        // back to the default `values/` resources for any language it has no translation for)
        // no call needed for that default case. A single small, synchronous DataStore read at
        // process start is the standard way to avoid a flash of the wrong language on cold start.
        val storedLanguageTag = runBlocking { languagePreferencesRepository.selectedLanguageTag.first() }
        if (storedLanguageTag != null) {
            localeController.applyLanguage(storedLanguageTag)
        }
    }
}
