package pt.achman.interfaces

import pt.achman.game.Game
import pt.achman.game.GameSource

interface RepositoryGame: Repository<Game> {
    fun createGame(
        externalGameId: String,
        name: String,
        source: GameSource,
    ): Game

    fun findByExternalId(externalGameId: String, source: GameSource): Game?
}