package pt.jsal.achman.providers.search

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.game.SearchedGame
import java.net.URLEncoder
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets

@Component
class PSNSearch(
    private val client: HttpClient,
) {
    private val mapper = jacksonObjectMapper()

    suspend fun searchGames(
        config: IntegrationsConfig,
        gameName: String,
    ): List<SearchedGame> {
        val encodedName = URLEncoder.encode(gameName, StandardCharsets.UTF_8)
        return emptyList()
    }
}
