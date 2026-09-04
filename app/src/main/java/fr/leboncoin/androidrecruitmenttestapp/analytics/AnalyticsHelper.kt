package fr.leboncoin.androidrecruitmenttestapp.analytics

/**
 * Deliberately an interface bound to [AnalyticsHelperImpl] via Hilt (see
 * [fr.leboncoin.androidrecruitmenttestapp.di.AnalyticsModule]) rather than a concrete class
 * injected directly: it lets ViewModel tests substitute a trivial fake instead of standing up a
 * real `Context`.
 */
interface AnalyticsHelper {
    fun trackScreenView(screenName: String)
    fun trackSelection(itemId: String)
}
