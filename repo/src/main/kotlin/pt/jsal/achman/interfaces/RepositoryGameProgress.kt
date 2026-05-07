package pt.jsal.achman.interfaces

import pt.jsal.achman.achievement.GameProgress

/**
 * Repositório de operações sobre o progresso de jogos de cada utilizador.
 */
interface RepositoryGameProgress : Repository<GameProgress> {
    /**
     * Cria um registo de progresso de jogo para um utilizador.
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     *
     * @return [GameProgress] criado.
     */
    fun createGameProgress(
        userId: Int,
        gameId: Int,
    ): GameProgress

    /**
     * Procura o progresso de um utilizador num jogo específico.
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     *
     * @return [GameProgress] correspondente, ou `null` caso não exista.
     */
    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): GameProgress?

    /**
     * Obtém todos os registos de progresso de jogos de um utilizador.
     *
     * @param userId Identificador do utilizador.
     *
     * @return Lista de [GameProgress] associados ao utilizador, ou lista vazia caso não existam.
     */
    fun findByUserId(userId: Int): List<GameProgress>

    /**
     * Adiciona uma achievement como concluída no progresso de um utilizador num jogo.
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     * @param achievementId Identificador da achievement concluída.
     *
     * @return [GameProgress] atualizado.
     */
    fun addCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress

    /**
     * Remove uma achievement concluída do progresso de um utilizador num jogo.
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     * @param achievementId Identificador da achievement a remover.
     *
     * @return [GameProgress] atualizado.
     */
    fun removeCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress

    /**
     * Remove todas as achievements concluídas de um utilizador num jogo.
     *
     * @param userId Identificador do utilizador.
     * @param gameId Identificador do jogo.
     *
     * @return [GameProgress] atualizado, ou `null` caso não exista registo de progresso.
     */
    fun clearCompletedAchievements(
        userId: Int,
        gameId: Int,
    ): GameProgress?

    /**
     * Remove todo o progresso de um utilizador
     *
     * @param userId Identificador do utilizador.
     */
    fun removeUserProgress(userId: Int)
}
