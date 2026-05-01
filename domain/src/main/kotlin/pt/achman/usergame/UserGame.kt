package pt.achman.usergame

/**
 * Representa a associação entre um utilizador e um jogo.
 *
 * Esta entidade permite indicar se o jogo deve ser sincronizado
 * automaticamente com a respetiva plataforma externa.
 *
 * @property id Identificador interno único da associação.
 * @property userId Identificador interno do utilizador.
 * @property gameId Identificador interno do jogo.
 * @property synchronize Indica se a sincronização automática está ativa.
 */
data class UserGame(
    val id: Int,
    val userId: Int,
    // internal game id
    val gameId: Int,
    val synchronize: Boolean = false,
)