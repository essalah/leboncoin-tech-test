package fr.leboncoin.androidrecruitmenttestapp.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject

class AppLocaleControllerImpl @Inject constructor() : AppLocaleController {

    override fun applyLanguage(languageTag: String) {
        // On API 33+ this talks straight to the platform LocaleManager, which recreates every
        // activity. On API 24-32, AppCompatDelegate's own backport does the equivalent, but only
        // auto-recreates AppCompatActivity subclasses hence MainActivity extending it.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}
