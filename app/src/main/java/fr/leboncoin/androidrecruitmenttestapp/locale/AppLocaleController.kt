package fr.leboncoin.androidrecruitmenttestapp.locale

/**
 * Thin seam around the actual Android per-app-language API, so [SettingsViewModel]
 * [fr.leboncoin.androidrecruitmenttestapp.ui.settings.SettingsViewModel] and [PhotoApp]
 * [fr.leboncoin.androidrecruitmenttestapp.PhotoApp] can be unit-tested with a fake instead of
 * exercising `AppCompatDelegate` (an Android framework call that isn't stubbed in a plain JUnit
 * test).
 */
interface AppLocaleController {

    /** Applies [languageTag] (e.g. "en", "fr") as the app's locale, overriding the device's. */
    fun applyLanguage(languageTag: String)
}
