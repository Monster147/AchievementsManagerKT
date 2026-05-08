package pt.jsal.achman.providers.search

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import pt.jsal.achman.config.IntegrationsConfig
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
) {
    private val mapper = jacksonObjectMapper()

    suspend fun searchGames(
        config: IntegrationsConfig,
        gameName: String,
    ): List<SearchedGame> {
        val encodedName = URLEncoder.encode(gameName, StandardCharsets.UTF_8)
        val json =
            getJson(
                "https://store.steampowered.com/api/storesearch/?term=$encodedName&cc=us&l=en",
            )

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

    private suspend fun getJson(url: String): JsonNode =
        withContext(Dispatchers.IO) {
            val request =
                HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .build()

            val response =
                client.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray(),
                )

            val raw = response.body()
            println(String(raw, StandardCharsets.UTF_8))

            mapper.readTree(response.body())
        }
}

fun main() =
    runBlocking {
        val client = HttpClient.newHttpClient()

        val steamSearch =
            SteamSearch(client)

        val config =
            IntegrationsConfig(
                STEAM_API_KEY = "",
                STEAM_USERID = "",
                RETRO_API_KEY = "",
                RETRO_USERNAME = "",
                PSN_API_KEY = "",
            )

        val games =
            steamSearch.searchGames(
                config,
                "Assassin's Creed",
            )

        games.forEach {
            println(
                """
                Name: ${it.name}
                External ID: ${it.externalGameId}
                Cover: ${it.cover}
                Source: ${it.source}
                """.trimIndent(),
            )

            println("------------")
        }
    }
