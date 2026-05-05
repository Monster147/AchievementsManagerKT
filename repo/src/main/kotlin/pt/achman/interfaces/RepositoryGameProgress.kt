package pt.achman.interfaces

import pt.achman.achievement.GameProgress

interface RepositoryGameProgress : Repository<GameProgress> {
    fun createGameProgress(
        userId: Int,
        gameId: Int,
    ): GameProgress

    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): GameProgress?

    fun findByUserId(userId: Int): List<GameProgress>

    fun addCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress

    fun removeCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress

    fun clearCompletedAchievements(
        userId: Int,
        gameId: Int,
    ): GameProgress?
}
