package pt.jsal.achman.mem

import pt.jsal.achman.game.Game
import pt.jsal.achman.game.GameGenre
import pt.jsal.achman.game.GamePlatform
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.interfaces.RepositoryGame

class RepositoryGameMem : RepositoryGame {
    private val games = mutableListOf<Game>()

    override fun createGame(
        externalGameId: String,
        name: String,
        source: GameSource,
    ): Game =
        Game(
            id = games.size + 1,
            externalGameId = externalGameId,
            name = name,
            source = source,
        ).also { games.add(it) }

    override fun findByExternalId(
        externalGameId: String,
        source: GameSource,
    ): Game? = games.find { it.externalGameId == externalGameId && it.source == source }

    override fun updateGameInfo(
        game: Game,
        externalGameId: String?,
        name: String?,
        genres: List<GameGenre>?,
        platform: GamePlatform?,
        releaseYear: String?,
        source: GameSource?,
        cover: String?,
    ): Game {
        val updatedGame =
            game.copy(
                externalGameId = externalGameId ?: game.externalGameId,
                name = name ?: game.name,
                genre = genres ?: game.genre,
                platform = platform ?: game.platform,
                releaseYear = releaseYear ?: game.releaseYear,
                source = source ?: game.source,
                cover = cover ?: game.cover,
            )
        save(updatedGame)
        return updatedGame
    }

    override fun findById(id: Int): Game? = games.find { it.id == id }

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
