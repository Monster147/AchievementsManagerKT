package pt.jsal.achman.providers

import org.springframework.stereotype.Component
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.game.SearchedGame
import pt.jsal.achman.providers.search.PSNSearch
import pt.jsal.achman.providers.search.SteamSearch
import java.util.concurrent.ConcurrentHashMap

@Component
class SearchProvider(
    private val steamSearch: SteamSearch,
    private val psnSearch: PSNSearch
) {
    private val cache = ConcurrentHashMap<Int, SearchedGame>()

    suspend fun searchGames(
        config: IntegrationsConfig,
        gameName: String,
        source: GameSource
    ): List<SearchedGame> {
        cache.clear()
        when(source) {
            GameSource.STEAM ->{

            }
            else -> return emptyList()
        }
    }

    private fun addToCache(results: List<SearchedGame>) {
        results.forEachIndexed { index, game ->
            cache[index + 1] = game
        }
    }

    fun getCachedGame(id: Int): SearchedGame? = cache[id]
}