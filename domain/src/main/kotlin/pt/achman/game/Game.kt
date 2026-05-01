package pt.achman.game

/**
 * Representa um jogo registado na aplicação.
 *
 * @property id Identificador interno único do jogo.
 * @property externalGameId Identificador do jogo na plataforma ou serviço externo de origem.
 * @property name Nome do jogo.
 * @property genre Lista de géneros associados ao jogo.
 * @property platform Plataforma principal em que o jogo está disponível.
 * @property releaseYear Ano de lançamento do jogo.
 * @property source Fonte ou serviço de onde o jogo foi obtido.
 * @property cover URL ou caminho da imagem de capa do jogo.
 */
data class Game(
    val id: Int,
    val externalGameId: String,
    val name: String,
    val genre: List<GameGenre> = listOf(),
    val platform: GamePlatform = GamePlatform.UNDEFINED,
    val releaseYear: String = "",
    val source: GameSource = GameSource.UNDEFINED,
    val cover: String = "",
)
