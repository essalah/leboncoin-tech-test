package fr.leboncoin.data.repository

import fr.leboncoin.core.network.api.AlbumApiService
import fr.leboncoin.core.network.model.AlbumDto

class FakeAlbumApiService(
    private val response: () -> List<AlbumDto> = { emptyList() },
) : AlbumApiService {
    override suspend fun getAlbums(): List<AlbumDto> = response()
}
