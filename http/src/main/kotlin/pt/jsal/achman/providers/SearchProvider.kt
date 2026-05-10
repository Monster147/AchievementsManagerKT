package pt.jsal.achman.providers

import org.springframework.stereotype.Component
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.game.SearchedGame
import pt.jsal.achman.providers.search.PSNSearch
import pt.jsal.achman.providers.search.SteamSearch
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success
import java.util.concurrent.ConcurrentHashMap

sealed class SearchError {
    object NoGameFound : SearchError()
}

@Component
class SearchProvider(
    private val steamSearch: SteamSearch,
    private val psnSearch: PSNSearch,
) {
    private val cache = ConcurrentHashMap<Int, SearchedGame>()

    suspend fun searchGames(
        userId: Int,
        config: IntegrationsConfig,
        gameName: String,
        source: GameSource,
    ): Either<SearchError, List<SearchedGame>> {
        cache.clear()
        return when (source) {
            GameSource.STEAM -> {
                val results = steamSearch.searchGames(gameName)
                if (results.isEmpty()) return failure(SearchError.NoGameFound)
                addToCache(results)
                success(results)
            }

            GameSource.PSN -> {
                val results = psnSearch.searchGames(userId, config, gameName)
                if (results.isEmpty()) return failure(SearchError.NoGameFound)
                addToCache(results)
                success(results)
            }

            else -> failure(SearchError.NoGameFound)
        }
    }

    private fun addToCache(results: List<SearchedGame>) {
        results.forEachIndexed { index, game ->
            val updatedGame =
                game.copy(
                    id = index,
                )
            cache[index + 1] = updatedGame
        }
    }

    fun getCachedGame(id: Int): SearchedGame? = cache[id]

    fun getCache(): List<SearchedGame> = cache.values.toList()
}
