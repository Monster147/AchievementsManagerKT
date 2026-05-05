package pt.achman.mem

import pt.achman.achievement.GameProgress
import pt.achman.interfaces.RepositoryGameProgress

class RepositoryGameProgressMem : RepositoryGameProgress {
    private val progresses = mutableListOf<GameProgress>()

    override fun createGameProgress(
        userId: Int,
        gameId: Int,
    ): GameProgress =
        GameProgress(
            progresses.size + 1,
            userId,
            gameId,
        ).also {
            progresses.add(it)
        }

    override fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): GameProgress? = progresses.find { it.userId == userId && it.gameId == gameId }

    override fun findByUserId(userId: Int): List<GameProgress> = progresses.filter { it.userId == userId }

    override fun addCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress {
        val progress = findByUserIdAndGameId(userId, gameId) ?: createGameProgress(userId, gameId)
        if (!progress.completedAchievements.contains(achievementId)) {
            val updated =
                progress.copy(
                    completedAchievements = progress.completedAchievements + achievementId,
                )
            save(updated)
            return updated
        }
        return progress
    }

    override fun removeCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress {
        val progress = findByUserIdAndGameId(userId, gameId) ?: createGameProgress(userId, gameId)
        if (progress.completedAchievements.contains(achievementId)) {
            val updated =
                progress.copy(
                    completedAchievements = progress.completedAchievements - achievementId,
                )
            save(updated)
            return updated
        }
        return progress
    }

    override fun clearCompletedAchievements(
        userId: Int,
        gameId: Int,
    ): GameProgress? {
        val progress = findByUserIdAndGameId(userId, gameId) ?: return null
        val updated =
            progress.copy(
                completedAchievements = emptyList(),
            )
        save(updated)
        return updated
    }

    override fun findById(id: Int): GameProgress? = progresses.find { it.id == id }

    override fun findAll(): List<GameProgress> = progresses.toList()

    override fun save(entity: GameProgress) {
        progresses.removeIf { it.id == entity.id }
        progresses.add(entity)
    }

    override fun deleteById(id: Int) {
        progresses.removeIf { it.id == id }
    }

    override fun clear() {
        progresses.clear()
    }
}
