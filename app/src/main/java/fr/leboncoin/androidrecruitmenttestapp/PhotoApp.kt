package fr.leboncoin.androidrecruitmenttestapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * `@HiltAndroidApp` triggers Hilt's code generation and creates the app-level (Singleton)
 * dependency container. This replaces the old hand-rolled `AppDependenciesProvider` service
 * locator dependencies are now requested with `@Inject`/`hiltViewModel()` instead of every
 * Activity casting `application` and pulling from a shared bag of `by lazy` properties.
 */
@HiltAndroidApp
class PhotoApp : Application()
