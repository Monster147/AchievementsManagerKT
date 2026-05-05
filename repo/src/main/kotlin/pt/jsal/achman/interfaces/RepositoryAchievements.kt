package pt.jsal.achman.interfaces

import pt.jsal.achman.achievement.Achievement

/**
 * Repositório de operações sobre achievements (conquistas).
 */
interface RepositoryAchievements : pt.jsal.achman.interfaces.Repository<pt.jsal.achman.achievement.Achievement> {

    /**
     * Cria um achievement associada a um jogo.
     *
     * @param apiName Nome identificador da achievement na API externa.
     * @param name Nome da achievement.
     * @param icon Ícone representativo da achievement.
     * @param description Descrição da achievement.
     * @param gameId Identificador do jogo ao qual a achievement pertence.
     *
     * @return [Achievement] criada.
     */
    fun createAchievement(
        apiName: String,
        name: String,
        icon: String,
        description: String,
        gameId: Int,
    ): Achievement

    /**
     * Procura todas as achievements associadas a um jogo.
     *
     * @param gameId Identificador do jogo.
     *
     * @return Lista de [Achievement] associadas ao jogo, ou lista vazia caso não existam.
     */
    fun findByGameId(gameId: Int): List<Achievement>

    /**
     * Procura uma achievement pelo seu nome na API.
     *
     * @param apiName Nome identificador da achievement na API externa.
     *
     * @return [Achievement] correspondente, ou `null` caso não exista.
     */
    fun findByApiName(apiName: String): Achievement?

    /**
     * Remove todas as achievements associadas a um jogo.
     *
     * @param gameId Identificador do jogo.
     */
    fun removeAchievements(gameId: Int)
}
