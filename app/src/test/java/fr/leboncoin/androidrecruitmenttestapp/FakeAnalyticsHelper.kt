package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.androidrecruitmenttestapp.analytics.AnalyticsHelper

class FakeAnalyticsHelper : AnalyticsHelper {
    val trackedScreens = mutableListOf<String>()
    val trackedSelections = mutableListOf<String>()

    override fun trackScreenView(screenName: String) {
        trackedScreens += screenName
    }

    override fun trackSelection(itemId: String) {
        trackedSelections += itemId
    }
}
