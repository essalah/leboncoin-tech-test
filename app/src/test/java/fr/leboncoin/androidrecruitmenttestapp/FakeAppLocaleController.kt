package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.androidrecruitmenttestapp.locale.AppLocaleController

class FakeAppLocaleController : AppLocaleController {

    val appliedLanguageTags = mutableListOf<String>()

    override fun applyLanguage(languageTag: String) {
        appliedLanguageTags += languageTag
    }
}
