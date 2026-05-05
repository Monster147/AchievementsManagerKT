package pt.achman

import org.springframework.stereotype.Component
import pt.achman.achievement.Achievement
import pt.achman.interfaces.TransactionManager
import pt.achman.user.UserRole
import pt.achman.utils.Either
import pt.achman.utils.failure
import pt.achman.utils.success

sealed class AchievementError {
    data object AchievementAlreadyExists : AchievementError()

    data object UserNotAdmin : AchievementError()

    data object GameNotFound : AchievementError()
}

@Component
class AchievementService(
    private val trxManager: TransactionManager,
) {
    fun createAchievement(
        userId: Int,
        gameId: Int,
        apiName: String,
        name: String,
        description: String,
        icon: String,
    ): Either<AchievementError, Achievement> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(AchievementError.UserNotAdmin)
            repoGames.findById(gameId) ?: return@run failure(AchievementError.GameNotFound)
            val existingAchievement = repoAchievements.findByApiName(apiName)
            if (existingAchievement != null) return@run failure(AchievementError.AchievementAlreadyExists)
            val achievement =
                repoAchievements.createAchievement(
                    apiName = apiName,
                    name = name,
                    icon = icon,
                    description = description,
                    gameId = gameId,
                )
            success(achievement)
        }

    fun findByGameId(gameId: Int): List<Achievement> =
        trxManager.run {
            repoAchievements.findByGameId(gameId)
        }

    fun removeAchievements(
        userId: Int,
        gameId: Int,
    ): Either<AchievementError, Boolean> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(AchievementError.UserNotAdmin)
            repoGames.findById(gameId) ?: return@run failure(AchievementError.GameNotFound)
            repoAchievements.removeAchievements(gameId)
            success(true)
        }
}
