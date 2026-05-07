package pt.jsal.achman.mem

import pt.jsal.achman.achievement.Achievement
import pt.jsal.achman.interfaces.RepositoryAchievements

class RepositoryAchievementsMem : RepositoryAchievements {
    private val achievements = mutableListOf<Achievement>()
    private var nextId = 1

    override fun createAchievement(
        apiName: String,
        name: String,
        icon: String,
        description: String,
        gameId: Int,
    ): Achievement =
        Achievement(
            id = nextId++,
            name = name,
            description = description,
            gameId = gameId,
            apiName = apiName,
            icon = icon,
        ).also { achievements.add(it) }

    override fun findByGameId(gameId: Int): List<Achievement> = achievements.filter { it.gameId == gameId }

    override fun findByApiName(apiName: String): Achievement? = achievements.find { it.apiName == apiName }

    override fun removeAchievements(gameId: Int) {
        achievements.removeIf { it.gameId == gameId }
    }

    override fun findById(id: Int): Achievement? = achievements.find { it.id == id }

    override fun findAll(): List<Achievement> = achievements.toList()

    override fun save(entity: Achievement) {
        achievements.removeIf { it.id == entity.id }
        achievements.add(entity)
    }

    override fun deleteById(id: Int) {
        achievements.removeIf { it.id == id }
    }

    override fun clear() {
        achievements.clear()
    }
}
