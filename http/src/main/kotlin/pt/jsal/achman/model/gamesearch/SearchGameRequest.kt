package pt.jsal.achman.model.gamesearch

import pt.jsal.achman.game.GameSource

data class SearchGameRequest(
    val gameName: String,
    val source: GameSource,
)
