package pt.jsal.achman.providers.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.game.SearchedGame
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

@Component
class SteamSearch(
    private val client: HttpClient,
    private val mapper: ObjectMapper
) {
    suspend fun searchGames(gameName: String): List<SearchedGame> {
        val encodedName = URLEncoder.encode(gameName, StandardCharsets.UTF_8)
        val url = "https://store.steampowered.com/api/storesearch/?term=$encodedName&cc=us&l=en"
        val request =
            HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .build()

        val response =
            client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString(),
            ).await()

        val json = mapper.readTree(response.body())

        val items = json["items"] ?: return emptyList()

        return items.map { item ->
            SearchedGame(
                externalGameId = item["id"].asText(),
                name = item["name"].asText(),
                source = GameSource.STEAM,
                cover = item["tiny_image"].asText(),
            )
        }
    }
}
