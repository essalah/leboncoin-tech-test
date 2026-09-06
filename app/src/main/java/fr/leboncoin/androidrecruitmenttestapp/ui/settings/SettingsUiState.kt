package fr.leboncoin.androidrecruitmenttestapp.ui.settings

import fr.leboncoin.data.model.AppLanguage

/**
 * @param selectedLanguage `null` means the user has never made an explicit choice: the app is
 * just following the device's locale (falling back to English for a device language it has no
 * translation for), and neither option should show as selected.
 */
data class SettingsUiState(val selectedLanguage: AppLanguage? = null)
