package pt.jsal.achman.model.game

import pt.jsal.achman.game.GameGenre
import pt.jsal.achman.game.GamePlatform
import pt.jsal.achman.game.GameSource

data class UpdateGameInput(
    val externalGameId: String?,
    val name: String?,
    val genres: List<GameGenre>?,
    val platform: GamePlatform?,
    val releaseYear: String?,
    val source: GameSource?,
    val cover: String?,
)
