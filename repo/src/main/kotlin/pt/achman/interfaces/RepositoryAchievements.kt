package pt.achman.interfaces

import pt.achman.achievement.Achievement

interface RepositoryAchievements: Repository<Achievement> {
    fun createAchievement(
        apiName: String,
        name: String,
        icon: String,
        description: String,
        gameId: Int,
    ): Achievement

    fun findByGameId(gameId: Int): List<Achievement>
    fun findByApiName(apiName: String): Achievement?
}