package pt.jsal.achman.achievement

/**
 * Representa o progresso de um utilizador relativamente
 * às conquistas de um jogo.
 *
 * @property id Identificador interno único do registo de progresso.
 * @property userId Identificador interno do utilizador.
 * @property gameId Identificador interno do jogo.
 * @property completedAchievements Lista de identificadores das conquistas concluídas pelo utilizador.
 */
data class GameProgress(
    val id: Int,
    val userId: Int,
    val gameId: Int,
    val completedAchievements: List<Int> = emptyList(),
)
