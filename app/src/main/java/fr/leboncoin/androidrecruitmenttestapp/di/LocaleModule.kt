package fr.leboncoin.androidrecruitmenttestapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.androidrecruitmenttestapp.locale.AppLocaleController
import fr.leboncoin.androidrecruitmenttestapp.locale.AppLocaleControllerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class LocaleModule {

    @Binds
    abstract fun bindAppLocaleController(impl: AppLocaleControllerImpl): AppLocaleController
}
