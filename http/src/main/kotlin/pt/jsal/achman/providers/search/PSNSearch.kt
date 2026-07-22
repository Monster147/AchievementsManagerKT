package pt.jsal.achman.providers.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.game.SearchedGame
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.providers.psnutils.PSN_TROPHY_BASE_URL
import pt.jsal.achman.providers.psnutils.authenticate
import pt.jsal.achman.providers.psnutils.exchangeRefreshTokenForAuthTokens
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class PSNSearch(
    private val client: HttpClient,
    private val trxManager: TransactionManager,
    private val mapper: ObjectMapper
) {
    suspend fun searchGames(
        userId: Int,
        config: IntegrationsConfig,
        gameName: String,
    ): List<SearchedGame> {
        ensureAuthenticated(userId, config, client)

        if (config.authTokens != null) {
            val request =
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "$PSN_TROPHY_BASE_URL/v1/users/me/trophyTitles",
                        ),
                    )
                    .header("Authorization", "Bearer ${config.authTokens?.accessToken}")
                    .header("Content-Type", "application/json")
                    .GET()
                    .build()

            val response =
                client.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString(),
                ).await()

            if (response.statusCode() !in 200..299) {
                error("PSN request failed with status ${response.statusCode()}")
            }

            val json = mapper.readTree(response.body())

            val gamesNode = json["trophyTitles"]

            val games =
                gamesNode.map { g ->
                    SearchedGame(
                        externalGameId =
                            g["npCommunicationId"]
                                .asText()
                                .substringBefore("_00"),
                        name = g["trophyTitleName"].asText(),
                        cover = g["trophyTitleIconUrl"].asText(),
                        source = GameSource.PSN,
                    )
                }

            val keywords =
                gameName.lowercase()
                    .split("\\s+".toRegex())

            return games.filter {
                val lowerName = it.name.lowercase()

                keywords.all(lowerName::contains)
            }
        }

        return emptyList()
    }

    private suspend fun ensureAuthenticated(
        userId: Int,
        config: IntegrationsConfig,
        client: HttpClient,
    ) {
        if (config.authTokens == null || System.currentTimeMillis() >= config.tokenExpiresAt) {
            config.authTokens?.refreshToken?.let { refreshToken ->
                config.authTokens =
                    exchangeRefreshTokenForAuthTokens(
                        refreshToken,
                        client,
                        mapper,
                    )
            } ?: authenticate(config, client, mapper)

            trxManager.run {
                repoConfig.updateConfig(
                    userId,
                    config,
                )
            }
        }
    }
}
