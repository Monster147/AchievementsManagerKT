package pt.jsal.achman.providers

import org.springframework.stereotype.Component
import pt.jsal.achman.AchievementService
import pt.jsal.achman.achievement.Achievement
import pt.jsal.achman.config.IntegrationsConfig
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.providers.achievements.PSNAchievements
import pt.jsal.achman.providers.achievements.RetroAchievements
import pt.jsal.achman.providers.achievements.SteamAchievements
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success

sealed class GetAchievementError {
    object NoAchievementFound : GetAchievementError()
}

@Component
class AchievementsProvider(
    private val steamAchievements: SteamAchievements,
    private val psnAchievements: PSNAchievements,
    private val retroAchievements: RetroAchievements,
    private val achievementsService: AchievementService,
) {
    suspend fun getAchievements(
        userId: Int,
        config: IntegrationsConfig,
        internalGameId: Int,
        externalGameId: String,
        source: GameSource,
    ): Either<GetAchievementError, Boolean> {
        return when (source) {
            GameSource.STEAM -> {
                val steamAchievementsList = steamAchievements.getAchievements(config, externalGameId)
                if (steamAchievementsList.isEmpty()) return failure(GetAchievementError.NoAchievementFound)
                saveAchievements(userId, internalGameId, steamAchievementsList)
                success(true)
            }

            GameSource.PSN -> {
                val psnAchievementList = psnAchievements.getAchievements(userId, config, externalGameId)
                if (psnAchievementList.isEmpty()) return failure(GetAchievementError.NoAchievementFound)
                saveAchievements(userId, internalGameId, psnAchievementList)
                success(true)
            }

            GameSource.RETROACHIEVEMENTS -> {
                val retroAchievementList = retroAchievements.getAchievements(config, externalGameId)
                if (retroAchievementList.isEmpty()) return failure(GetAchievementError.NoAchievementFound)
                saveAchievements(userId, internalGameId, retroAchievementList)
                success(true)
            }

            else -> failure(GetAchievementError.NoAchievementFound)
        }
    }

    private suspend fun saveAchievements(
        userId: Int,
        internalGameId: Int,
        achievements: List<Achievement>,
    ) {
        for (achievement in achievements) {
            achievementsService.createAchievement(
                userId,
                internalGameId,
                achievement.apiName,
                achievement.name,
                achievement.description,
                achievement.icon,
            )
        }
    }
}
