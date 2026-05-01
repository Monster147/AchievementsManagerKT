package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.game.Game
import pt.achman.game.GameSource
import pt.achman.interfaces.RepositoryGame

class RepositoryGameJdbi(
    handle: Handle
): RepositoryGame {
    override fun createGame(
        externalGameId: String,
        name: String,
        source: GameSource
    ): Game {
        TODO("Not yet implemented")
    }

    override fun findByExternalId(
        externalGameId: String,
        source: GameSource
    ): Game? {
        TODO("Not yet implemented")
    }

    override fun findById(id: Int): Game? {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<Game> {
        TODO("Not yet implemented")
    }

    override fun save(entity: Game) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Int) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}