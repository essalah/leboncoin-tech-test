package fr.leboncoin.data.model

/** The languages the app ships translations for, and can be explicitly switched to from Settings. */
enum class AppLanguage(val languageTag: String) {
    ENGLISH("en"),
    FRENCH("fr"),
}
