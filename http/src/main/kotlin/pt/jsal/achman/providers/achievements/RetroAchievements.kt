package pt.jsal.achman.providers.achievements

import com.haroldadmin.cnradapter.NetworkResponse
import org.retroachivements.api.RetroClient
import org.retroachivements.api.RetroInterface
import org.retroachivements.api.data.RetroCredentials
import org.retroachivements.api.data.pojo.ErrorResponse
import org.retroachivements.api.data.pojo.game.GetGameExtended
import org.springframework.stereotype.Component
import pt.jsal.achman.achievement.Achievement
import pt.jsal.achman.config.IntegrationsConfig
import java.net.http.HttpClient

@Component
class RetroAchievements(
    private val client: HttpClient,
) {
    suspend fun getAchievements(
        config: IntegrationsConfig,
        externalGameId: String,
    ): List<Achievement> {
        if (config.RETRO_USERNAME.isBlank() || config.RETRO_API_KEY.isBlank()) {
            return emptyList()
        }

        val credentials = RetroCredentials(config.RETRO_USERNAME, config.RETRO_API_KEY)
        val api = RetroClient(credentials).api

        val response: NetworkResponse<GetGameExtended.Response, ErrorResponse> =
            api.getGameExtended(
                gameId = externalGameId.toLong(),
            )

        if (response is NetworkResponse.Success) {
            val gameExtended: GetGameExtended.Response = response.body
            println("Game extended" + gameExtended.achievements.values)
            val achievements =
                gameExtended.achievements.values
                    .sortedBy { it.displayOrder }
                    .map { ach ->
                        Achievement(
                            id = 0,
                            apiName = ach.id.toString(),
                            name = ach.title,
                            icon = "https://media.retroachievements.org/Badge/${ach.badgeName}.png",
                            description = ach.description ?: "",
                            gameId = 0,
                        )
                    }
            return achievements
        }

        return emptyList()
    }
}
