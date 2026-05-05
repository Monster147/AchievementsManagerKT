package pt.achman

import org.springframework.stereotype.Component
import pt.achman.interfaces.TransactionManager
import pt.achman.usergame.UserGame
import pt.achman.utils.Either
import pt.achman.utils.failure
import pt.achman.utils.success

sealed class UserGamesError {
    data object UserGameAlreadyExists : UserGamesError()

    data object UserGameNotFound : UserGamesError()

    data object UserNotFound : UserGamesError()
}

@Component
class UserGamesService(
    private val trxManager: TransactionManager,
) {
    fun createUserGame(
        userId: Int,
        gameId: Int,
    ): Either<UserGamesError, UserGame> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(UserGamesError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(UserGamesError.UserGameNotFound)
            val existing = repoUserGames.findByUserIdAndGameId(userId, gameId)
            if (existing != null) return@run failure(UserGamesError.UserGameAlreadyExists)
            val userGame = repoUserGames.createUserGame(userId, gameId, synchronize = false)
            success(userGame)
        }

    fun findByUserId(userId: Int): List<UserGame> =
        trxManager.run {
            repoUserGames.findByUserId(userId)
        }

    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): Either<UserGamesError, UserGame> =
        trxManager.run {
            val userGame = repoUserGames.findByUserIdAndGameId(userId, gameId)
            if (userGame == null) {
                failure(UserGamesError.UserGameNotFound)
            } else {
                success(userGame)
            }
        }

    fun alterSyncOption(
        userId: Int,
        gameId: Int,
    ): Either<UserGamesError, UserGame> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(UserGamesError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(UserGamesError.UserGameNotFound)
            val userGame = repoUserGames.findByUserIdAndGameId(userId, gameId)
            if (userGame == null) {
                failure(UserGamesError.UserGameNotFound)
            } else {
                val userGameUpdated = repoUserGames.alterSyncOption(userGame)
                success(userGameUpdated)
            }
        }
}
