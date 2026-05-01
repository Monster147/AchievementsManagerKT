package pt.achman.mem

import pt.achman.game.Game
import pt.achman.game.GameSource
import pt.achman.interfaces.RepositoryGame

class RepositoryGameMem: RepositoryGame {
    private val games = mutableListOf<Game>()

    override fun createGame(
        externalGameId: String,
        name: String,
        source: GameSource
    ): Game =
        Game(
            id = games.size + 1,
            externalGameId = externalGameId,
            name = name,
            source = source,
        ).also { games.add(it) }

    override fun findByExternalId(
        externalGameId: String,
        source: GameSource
    ): Game? =
        games.find { it.externalGameId == externalGameId && it.source == source }

    override fun findById(id: Int): Game? =
        games.find { it.id == id }

    override fun findAll(): List<Game> = games.toList()

    override fun save(entity: Game) {
        games.removeIf { it.id == entity.id }
        games.add(entity)
    }

    override fun deleteById(id: Int) {
        games.removeIf { it.id == id }
    }

    override fun clear() {
        games.clear()
    }
}