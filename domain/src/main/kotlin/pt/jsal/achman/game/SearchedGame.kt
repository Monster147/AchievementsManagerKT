package pt.jsal.achman.game

data class SearchedGame(
    val externalGameId: String,
    val name: String,
    val source: GameSource = GameSource.UNDEFINED,
    val cover: String = "",
)