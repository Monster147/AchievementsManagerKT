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
    private lateinit var retroCredentials: RetroCredentials

    private lateinit var api: RetroInterface

    suspend fun getAchievements(
        config: IntegrationsConfig,
        externalGameId: String,
    ): List<Achievement> {
        if (!::api.isInitialized) {
            if (config.RETRO_USERNAME.isNotEmpty() || config.RETRO_API_KEY.isNotEmpty()) {
                retroCredentials = RetroCredentials(config.RETRO_USERNAME, config.RETRO_API_KEY)
                api = RetroClient(retroCredentials).api
            } else {
                return emptyList()
            }
        }

        val response: NetworkResponse<GetGameExtended.Response, ErrorResponse> =
            api.getGameExtended(
                gameId = externalGameId.toLong(),
            )

        if (response is NetworkResponse.Success) {
            val gameExtended: GetGameExtended.Response = response.body

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
