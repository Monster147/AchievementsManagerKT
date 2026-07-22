package pt.jsal.achman

import org.springframework.stereotype.Component
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.userstats.UserStats
import pt.jsal.achman.userstats.UserGameStats
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success

sealed class UserStatsError {
    data object UserNotFound : UserStatsError()
}

@Component
class UserStatsService(
    private val trxManager: TransactionManager,
) {
    fun getUserStats(userId: Int): Either<UserStatsError, UserStats> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(UserStatsError.UserNotFound)

            val userGames = repoUserGames.findByUserId(userId)

            val perGameStats = userGames.mapNotNull { ug ->
                val game = repoGames.findById(ug.gameId) ?: return@mapNotNull null
                val total = repoAchievements.findByGameId(ug.gameId).size
                val unlocked = repoGameProgress
                    .findByUserIdAndGameId(userId, ug.gameId)
                    ?.completedAchievements?.size ?: 0

                UserGameStats(
                    gameId = game.id,
                    gameName = game.name,
                    totalAchievements = total,
                    unlockedAchievements = unlocked,
                    lockedAchievements = total - unlocked,
                    completionPercentage = if (total == 0) 0f else unlocked * 100f / total,
                )
            }

            val totalAch = perGameStats.sumOf { it.totalAchievements }
            val unlockedAch = perGameStats.sumOf { it.unlockedAchievements }

            success(
                UserStats(
                    totalGames = perGameStats.size,
                    gamesWithAchievements = perGameStats.count { it.totalAchievements > 0 },
                    totalAchievements = totalAch,
                    unlockedAchievements = unlockedAch,
                    lockedAchievements = totalAch - unlockedAch,
                    completionPercentage = if (totalAch == 0) 0f else unlockedAch * 100f / totalAch,
                    perGameStats = perGameStats,
                )
            )
        }
}