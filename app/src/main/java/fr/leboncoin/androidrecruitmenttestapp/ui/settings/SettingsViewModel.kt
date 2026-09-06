package fr.leboncoin.androidrecruitmenttestapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.androidrecruitmenttestapp.locale.AppLocaleController
import fr.leboncoin.data.model.AppLanguage
import fr.leboncoin.data.repository.LanguagePreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LanguagePreferencesRepository,
    private val localeController: AppLocaleController,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = repository.selectedLanguageTag
        .map { tag -> SettingsUiState(selectedLanguage = AppLanguage.entries.find { it.languageTag == tag }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SettingsUiState(),
        )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguageTag(language.languageTag) }
        // Applied right away rather than waiting on the DataStore write above, so the UI switches
        // immediately; PhotoApp re-applies whatever ends up persisted on the next cold start
        // regardless, so this can't drift out of sync with the stored value.
        localeController.applyLanguage(language.languageTag)
    }
}
