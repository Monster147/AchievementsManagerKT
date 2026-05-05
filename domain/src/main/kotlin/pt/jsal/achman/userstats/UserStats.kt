package pt.jsal.achman.userstats

data class UserStats(
    val totalGames: Int,
    val gamesWithAchievements: Int,
    val totalAchievements: Int,
    val unlockedAchievements: Int,
    val lockedAchievements: Int,
    val completionPercentage: Float,
    val perGameStats: List<UserGameStats>,
)
