package pt.jsal.achman.model.game

import pt.jsal.achman.game.GameSource

data class CreateGameInput(
    val externalGameId: String,
    val name: String,
    val source: GameSource,
)
