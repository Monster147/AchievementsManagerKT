package pt.achman.userstats

data class UserGameStats(
    val gameId: Int,
    val gameName: String,
    val totalAchievements: Int,
    val unlockedAchievements: Int,
    val lockedAchievements: Int,
    val completionPercentage: Float,
)
