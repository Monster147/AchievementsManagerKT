package pt.jsal.achman.model.game

import pt.jsal.achman.game.GameSource

data class FindExternalIdInput(
    val externalId: String,
    val source: GameSource,
)
