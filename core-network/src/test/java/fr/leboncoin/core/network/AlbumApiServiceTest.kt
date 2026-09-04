package fr.leboncoin.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import fr.leboncoin.core.network.api.AlbumApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.create

/**
 * Exercises the real Retrofit + kotlinx.serialization wiring against a fixture that mirrors the
 * actual response shape of https://static.leboncoin.fr/img/shared/technical-test.json (captured
 * as core-network/src/test/resources/albums.json), instead of trusting the DTO by inspection
 * alone.
 */
class AlbumApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AlbumApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }

        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        api = retrofit.create()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getAlbums parses the real endpoint schema`() = runTest {
        val fixture = checkNotNull(javaClass.classLoader?.getResourceAsStream("albums.json"))
            .bufferedReader()
            .readText()
        server.enqueue(MockResponse().setBody(fixture).setResponseCode(200))

        val albums = api.getAlbums()

        assertEquals(3, albums.size)
        assertEquals(1, albums[0].id)
        assertEquals(1, albums[0].albumId)
        assertEquals(
            "accusamus beatae ad facilis cum similique qui sunt",
            albums[0].title,
        )
        assertEquals(51, albums[2].id)
        assertEquals(2, albums[2].albumId)
    }

    @Test
    fun `getAlbums ignores unknown fields instead of crashing`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """[{"id":1,"albumId":1,"title":"t","url":"u","thumbnailUrl":"tu","unexpectedField":true}]""",
            ).setResponseCode(200),
        )

        val albums = api.getAlbums()

        assertEquals(1, albums.size)
    }
}
