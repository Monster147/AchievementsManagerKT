package pt.achman.interfaces

import pt.achman.game.Game
import pt.achman.game.GameGenre
import pt.achman.game.GamePlatform
import pt.achman.game.GameSource

interface RepositoryGame : Repository<Game> {
    fun createGame(
        externalGameId: String,
        name: String,
        source: GameSource,
    ): Game

    fun findByExternalId(
        externalGameId: String,
        source: GameSource,
    ): Game?

    fun updateGameInfo(
        game: Game,
        externalGameId: String?,
        name: String?,
        genres: List<GameGenre>?,
        platform: GamePlatform?,
        releaseYear: String?,
        source: GameSource?,
        cover: String?,
    ): Game
}
