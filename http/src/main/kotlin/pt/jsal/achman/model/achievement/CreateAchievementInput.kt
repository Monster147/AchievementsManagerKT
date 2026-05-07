package pt.jsal.achman.model.achievement

data class CreateAchievementInput(
    val gameId: Int,
    val apiName: String,
    val name: String,
    val description: String,
    val icon: String,
)
