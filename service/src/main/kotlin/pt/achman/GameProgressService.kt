package pt.achman

import org.springframework.stereotype.Component
import pt.achman.achievement.GameProgress
import pt.achman.interfaces.TransactionManager
import pt.achman.utils.Either
import pt.achman.utils.failure
import pt.achman.utils.success

sealed class GameProgressError {
    data object GameNotFound : GameProgressError()

    data object AchievementNotFound : GameProgressError()

    data object UserNotFound : GameProgressError()

    data object ProgressNotFound : GameProgressError()
}

@Component
class GameProgressService(
    private val trxManager: TransactionManager,
) {
    fun createGameProgress(
        userId: Int,
        gameId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            val progress = repoGameProgress.createGameProgress(userId, gameId)
            success(progress)
        }

    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            val progress = repoGameProgress.findByUserIdAndGameId(userId, gameId)
            if (progress == null) return@run failure(GameProgressError.ProgressNotFound)
            success(progress)
        }

    fun findByUserId(userId: Int): List<GameProgress> =
        trxManager.run {
            repoGameProgress.findByUserId(userId)
        }

    fun addCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            repoAchievements.findById(achievementId) ?: return@run failure(GameProgressError.AchievementNotFound)
            val progress = repoGameProgress.addCompletedAchievement(userId, gameId, achievementId)
            success(progress)
        }

    fun removeCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            repoAchievements.findById(achievementId) ?: return@run failure(GameProgressError.AchievementNotFound)
            val progress = repoGameProgress.removeCompletedAchievement(userId, gameId, achievementId)
            success(progress)
        }

    fun clearCompletedAchievements(
        userId: Int,
        gameId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            val progress =
                repoGameProgress.clearCompletedAchievements(userId, gameId) ?: return@run failure(
                    GameProgressError.ProgressNotFound,
                )
            success(progress)
        }
}
