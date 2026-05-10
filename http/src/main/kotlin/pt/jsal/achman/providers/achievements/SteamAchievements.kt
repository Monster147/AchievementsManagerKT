package pt.jsal.achman.providers.achievements

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import pt.jsal.achman.achievement.Achievement
import pt.jsal.achman.config.IntegrationsConfig
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class SteamAchievements(
    private val client: HttpClient,
) {
    private val mapper = jacksonObjectMapper()

    suspend fun getAchievements(
        config: IntegrationsConfig,
        externalGameId: String,
    ): List<Achievement> {
        val url =
            buildString {
                append("https://api.steampowered.com/ISteamUserStats/GetSchemaForGame/v2/")
                append("?key=${config.STEAM_API_KEY}")
                append("&appid=$externalGameId")
                append("&l=portuguese")
            }

        val request =
            HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(url))
                .build()

        val response =
            client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString(),
            ).await()

        if (response.statusCode() !in 200..299) {
            return emptyList()
        }

        val json = mapper.readTree(response.body())

        val achievementsNode =
            json["game"]
                ?.get("availableGameStats")
                ?.get("achievements")
                ?: return emptyList()

        val achievements =
            achievementsNode.map { node ->
                Achievement(
                    id = 0,
                    apiName = node["name"].asText(),
                    name = node["displayName"].asText(),
                    icon = node["icon"]?.asText() ?: "",
                    description = node["description"]?.asText() ?: "",
                    gameId = 0,
                )
            }.sortedWith(
                compareBy<Achievement> {
                    it.apiName.toIntOrNull() ?: Int.MAX_VALUE
                }.thenBy {
                    it.apiName
                },
            )

        return achievements
    }
}
