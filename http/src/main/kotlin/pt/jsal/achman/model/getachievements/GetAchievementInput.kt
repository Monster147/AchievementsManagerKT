package pt.jsal.achman.model.getachievements

import pt.jsal.achman.game.GameSource

data class GetAchievementInput(
    val internalGameId: Int,
    val externalGameId: String,
    val source: GameSource,
)
