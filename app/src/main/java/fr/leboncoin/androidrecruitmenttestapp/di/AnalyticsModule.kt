package fr.leboncoin.androidrecruitmenttestapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.androidrecruitmenttestapp.analytics.AnalyticsHelper
import fr.leboncoin.androidrecruitmenttestapp.analytics.AnalyticsHelperImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    abstract fun bindAnalyticsHelper(impl: AnalyticsHelperImpl): AnalyticsHelper
}
