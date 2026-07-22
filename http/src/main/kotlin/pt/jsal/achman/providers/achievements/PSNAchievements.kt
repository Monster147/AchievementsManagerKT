package pt.jsal.achman.providers.achievements

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.future.await
import org.springframework.beans.factory.ObjectFactory
import org.springframework.stereotype.Component
import pt.jsal.achman.achievement.Achievement
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.providers.psnutils.PSN_TROPHY_BASE_URL
import pt.jsal.achman.providers.psnutils.authenticate
import pt.jsal.achman.providers.psnutils.exchangeRefreshTokenForAuthTokens
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class PSNAchievements(
    private val client: HttpClient,
    private val trxManager: TransactionManager,
    private val mapper: ObjectMapper
) {
    suspend fun getAchievements(
        userId: Int,
        config: IntegrationsConfig,
        externalGameId: String,
    ): List<Achievement> {
        ensureAuthenticated(userId, config, client)
        val fullExternalGameId = "${externalGameId}_00"
        if (config.authTokens != null) {
            val url = "$PSN_TROPHY_BASE_URL/v1/npCommunicationIds/$fullExternalGameId/trophyGroups/all/trophies?npServiceName=trophy"
            val request =
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer ${config.authTokens?.accessToken}")
                    .header("Content-Type", "application/json")
                    .header("Accept-Language", "pt-PT")
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

            val trophiesNode =
                json["trophies"]
                    ?: return emptyList()

            val trophies =
                trophiesNode.map { t ->
                    Achievement(
                        id = 0,
                        apiName = t["trophyId"].asText(),
                        name = t["trophyName"].asText(),
                        icon = t["trophyIconUrl"].asText(),
                        description = t["trophyDetail"].asText(),
                        gameId = 0,
                    )
                }

            return trophies
        } else {
            return emptyList()
        }
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
