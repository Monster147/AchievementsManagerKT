package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.achievement.Achievement
import pt.achman.interfaces.RepositoryAchievements

class RepositoryAchievementsJdbi(
    handle: Handle
): RepositoryAchievements {
    override fun createAchievement(
        apiName: String,
        name: String,
        icon: String,
        description: String,
        gameId: Int
    ): Achievement {
        TODO("Not yet implemented")
    }

    override fun findByGameId(gameId: Int): List<Achievement> {
        TODO("Not yet implemented")
    }

    override fun findByApiName(apiName: String): Achievement? {
        TODO("Not yet implemented")
    }

    override fun findById(id: Int): Achievement? {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<Achievement> {
        TODO("Not yet implemented")
    }

    override fun save(entity: Achievement) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Int) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}