package pt.achman.mem

import pt.achman.achievement.Achievement
import pt.achman.interfaces.RepositoryAchievements

class RepositoryAchievementsMem : RepositoryAchievements {
    private val achievements = mutableListOf<Achievement>()

    override fun createAchievement(
        apiName: String,
        name: String,
        icon: String,
        description: String,
        gameId: Int,
    ): Achievement =
        Achievement(
            id = achievements.size + 1,
            name = name,
            description = description,
            gameId = gameId,
            apiName = apiName,
            icon = icon,
        ).also { achievements.add(it) }

    override fun findByGameId(gameId: Int): List<Achievement> = achievements.filter { it.gameId == gameId }

    override fun findByApiName(apiName: String): Achievement? = achievements.find { it.apiName == apiName }

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
