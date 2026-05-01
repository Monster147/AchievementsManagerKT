package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.achievement.GameProgress
import pt.achman.interfaces.RepositoryGameProgress

class RepositoryGameProgressJdbi(
    handle: Handle
): RepositoryGameProgress {
    override fun createGameProgress(userId: Int, gameId: Int): GameProgress {
        TODO("Not yet implemented")
    }

    override fun findByUserIdAndGameId(userId: Int, gameId: Int): GameProgress? {
        TODO("Not yet implemented")
    }

    override fun findByUserId(userId: Int): List<GameProgress> {
        TODO("Not yet implemented")
    }

    override fun addCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int
    ): GameProgress {
        TODO("Not yet implemented")
    }

    override fun removeCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int
    ): GameProgress {
        TODO("Not yet implemented")
    }

    override fun findById(id: Int): GameProgress? {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<GameProgress> {
        TODO("Not yet implemented")
    }

    override fun save(entity: GameProgress) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Int) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}