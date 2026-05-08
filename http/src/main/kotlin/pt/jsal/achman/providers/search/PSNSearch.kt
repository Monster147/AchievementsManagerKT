package pt.jsal.achman.providers.search

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import pt.jsal.achman.achievement.Achievement
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.game.SearchedGame
import pt.jsal.achman.providers.psnutils.AuthTokensResponse
import pt.jsal.achman.providers.psnutils.exchangeAccessCodeForToken
import pt.jsal.achman.providers.psnutils.exchangeNpssoForAccessCode
import pt.jsal.achman.providers.psnutils.exchangeRefreshTokenForAuthTokens
import java.net.URLEncoder
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Component
class PSNSearch(
    private val client: HttpClient,
) {
    private var authTokens: AuthTokensResponse? = null
    private var tokenExpiresAt: Long = 0
    private val mapper = jacksonObjectMapper()

    suspend fun searchGames(
        config: IntegrationsConfig,
        gameName: String,
    ): List<SearchedGame> {
        ensureAuthenticated(config)

        val request =
            java.net.http.HttpRequest.newBuilder()
                .uri(
                    java.net.URI.create(
                        "https://m.np.playstation.com/api/trophy/v1/users/me/trophyTitles",
                    ),
                )
                .header("Authorization", "Bearer ${authTokens!!.accessToken}")
                .header("Content-Type", "application/json")
                .GET()
                .build()

        val response =
            client.sendAsync(
                request,
                java.net.http.HttpResponse.BodyHandlers.ofString(),
            ).await()

        if (response.statusCode() !in 200..299) {
            error("PSN request failed with status ${response.statusCode()}")
        }

        val json = mapper.readTree(response.body())

        val gamesNode = json["trophyTitles"]

        val games =
            gamesNode.map { g ->
                SearchedGame(
                    externalGameId = g["npCommunicationId"]
                        .asText()
                        .substringBefore("_00"),
                    name = g["trophyTitleName"].asText(),
                    cover = g["trophyTitleIconUrl"].asText(),
                    source = GameSource.PSN
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

    private suspend fun ensureAuthenticated(config: IntegrationsConfig) {
        if (authTokens == null || System.currentTimeMillis() >= tokenExpiresAt) {
            authTokens?.refreshToken?.let { refreshToken ->
                authTokens =
                    exchangeRefreshTokenForAuthTokens(
                        refreshToken,
                        client,
                    )
            } ?: authenticate(config)
        }
    }

    private suspend fun authenticate(config: IntegrationsConfig) {
        val authCode = exchangeNpssoForAccessCode(config.PSN_API_KEY, client)
        val tokens = exchangeAccessCodeForToken(authCode, client)

        authTokens = tokens
        tokenExpiresAt = System.currentTimeMillis() + tokens.expiresIn * 1000
    }
}