package fr.leboncoin.androidrecruitmenttestapp.ui.detail

import fr.leboncoin.data.model.Album

data class DetailUiState(
    val album: Album? = null,
    val isLoading: Boolean = true,
)
