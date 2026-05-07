package pt.jsal.achman.model.gameprogress

data class CompletedAchievementInput(
    val userId: Int,
    val gameId: Int,
    val achievementId: Int,
)
