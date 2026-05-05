package pt.achman

import org.springframework.stereotype.Component
import pt.achman.game.Game
import pt.achman.game.GameGenre
import pt.achman.game.GamePlatform
import pt.achman.game.GameSource
import pt.achman.interfaces.TransactionManager
import pt.achman.user.UserRole
import pt.achman.utils.Either
import pt.achman.utils.failure
import pt.achman.utils.success

sealed class GameError {
    data object GameAlreadyExists : GameError()

    data object GameNotFound : GameError()

    data object UserNotAdmin : GameError()
}

@Component
class GameService(
    private val trxManager: TransactionManager,
) {
    fun createGame(
        userId: Int,
        externalGameId: String,
        name: String,
        source: GameSource,
    ): Either<GameError, Game> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(GameError.UserNotAdmin)
            val existingGame = repoGames.findByExternalId(externalGameId, source)
            if (existingGame != null) return@run failure(GameError.GameAlreadyExists)
            val newGame = repoGames.createGame(externalGameId, name, source)
            success(newGame)
        }

    fun findByExternalId(
        externalGameId: String,
        source: GameSource,
    ): Either<GameError, Game> =
        trxManager.run {
            val game = repoGames.findByExternalId(externalGameId, source)
            if (game == null) {
                failure(GameError.GameNotFound)
            } else {
                success(game)
            }
        }

    fun updateGameInfo(
        userId: Int,
        gameId: Int,
        externalGameId: String?,
        name: String?,
        genres: List<GameGenre>?,
        platform: GamePlatform?,
        releaseYear: String?,
        source: GameSource?,
        cover: String?,
    ): Either<GameError, Game> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(GameError.UserNotAdmin)
            val game = repoGames.findById(gameId) ?: return@run failure(GameError.GameNotFound)
            val updatedGame =
                repoGames.updateGameInfo(
                    game,
                    externalGameId,
                    name,
                    genres,
                    platform,
                    releaseYear,
                    source,
                    cover,
                )
            success(updatedGame)
        }

    fun findById(gameId: Int): Either<GameError, Game> =
        trxManager.run {
            val game = repoGames.findById(gameId)
            if (game == null) {
                return@run failure(GameError.GameNotFound)
            } else {
                return@run success(game)
            }
        }

    fun findAll(): List<Game> = trxManager.run { repoGames.findAll() }

    fun deleteById(
        userId: Int,
        gameId: Int,
    ): Either<GameError, Boolean> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(GameError.UserNotAdmin)
            repoGames.findById(gameId) ?: return@run failure(GameError.GameNotFound)
            repoGames.deleteById(gameId)
            success(true)
        }
}
