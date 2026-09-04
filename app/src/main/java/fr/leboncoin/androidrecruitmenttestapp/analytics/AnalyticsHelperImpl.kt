package fr.leboncoin.androidrecruitmenttestapp.analytics

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelperImpl @Inject constructor(
    // the previous version stored whatever Context was passed to a mutable
    // `initialize(context)` setter, and both Activities called it with `this` (an Activity
    // context) since this class is an app-scoped singleton, it retained an Activity past its
    // lifecycle, a classic leak (exactly the kind LeakCanary, already a debug dependency here,
    // exists to catch). Injecting `@ApplicationContext` through the constructor makes that bug
    // structurally impossible: there is no setter, and only the process-wide Context can ever
    // be provided here.
    @param:ApplicationContext private val context: Context,
) : AnalyticsHelper {

    override fun trackSelection(itemId: String) {
        val prefs = context.getSharedPreferences(ANALYTICS_SHARED_PREFS, Context.MODE_PRIVATE)
        prefs.edit { putString(SELECTED_ITEM_KEY, itemId) }
        Log.d(TAG, "User selected item - $itemId")
    }

    override fun trackScreenView(screenName: String) {
        Log.d(TAG, "Screen viewed - $screenName")
    }

    private companion object {
        const val TAG = "AnalyticsHelper"
        const val ANALYTICS_SHARED_PREFS = "analytics_prefs"
        const val SELECTED_ITEM_KEY = "selected_item"
    }
}
