package pt.jsal.achman.interfaces

import pt.jsal.achman.usergame.UserGame

/**
 * Repositório de operações sobre a associação entre utilizadores e jogos.
 */
interface RepositoryUserGames : Repository<UserGame> {
    /**
     * Cria uma associação entre um utilizador e um jogo.
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     * @param synchronize Indica se o jogo deve ser sincronizado automaticamente.
     *
     * @return [UserGame] criado.
     */
    fun createUserGame(
        userId: Int,
        gameId: Int,
        synchronize: Boolean,
    ): UserGame

    /**
     * Obtém todos os jogos associados a um utilizador.
     *
     * @param userId Identificador do utilizador.
     *
     * @return Lista de [UserGame] associados ao utilizador, ou lista vazia caso não existam.
     */
    fun findByUserId(userId: Int): List<UserGame>

    /**
     * Procura a associação entre um utilizador e um jogo específico.
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     *
     * @return [UserGame] correspondente, ou null caso não exista.
     */
    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): UserGame?

    /**
     * Altera a opção de sincronização de um jogo associado a um utilizador.
     *
     * @param userGame Associação utilizador-jogo a atualizar.
     *
     * @return [UserGame] atualizado.
     */
    fun alterSyncOption(userGame: UserGame): UserGame

    /**
     * Remove todos os jogos da biblioteca de um utilizador.
     *
     * @param userId Identificador do utilizador.
     */
    fun removeUserGames(userId: Int)

    /**
     * Remove um jogo da biblioteca de um utilizador
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     */
    fun removeGame(
        userId: Int,
        gameId: Int,
    )
}
