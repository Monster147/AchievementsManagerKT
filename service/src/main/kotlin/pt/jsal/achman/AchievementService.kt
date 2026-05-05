package pt.jsal.achman

import org.springframework.stereotype.Component
import pt.jsal.achman.achievement.Achievement
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.user.UserRole
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success

/**
 * Representa os possíveis erros associados às operações sobre conquistas (achievements).
 */
sealed class AchievementError {
    /**
     * Indica que já existe uma conquista com o mesmo `apiName`.
     */
    data object AchievementAlreadyExists : AchievementError()

    /**
     * Indica que o utilizador não tem permissões de administrador.
     */
    data object UserNotAdmin : AchievementError()

    /**
     * Indica que o jogo especificado não foi encontrado.
     */
    data object GameNotFound : AchievementError()
}

/**
 * Serviço responsável pela gestão de conquistas (achievements).
 *
 * Responsabilidades principais:
 * - criação de conquistas associadas a jogos;
 * - consulta de conquistas por jogo;
 * - remoção de conquistas de um jogo;
 * - validação de permissões de utilizador (admin);
 * - validação de existência de entidades relacionadas (jogos).
 *
 * Todas as operações são executadas dentro de uma transação, através do [TransactionManager].
 *
 * @param trxManager gestor de transações usado para aceder aos repositórios dentro de unidades de trabalho.
 */
@Component
class AchievementService(
    private val trxManager: TransactionManager,
) {
    /**
     * Cria uma conquista associada a um jogo.
     *
     * Regras de validação:
     * - o utilizador tem de existir e ser administrador;
     * - o jogo tem de existir;
     * - não pode existir outra conquista com o mesmo `apiName`.
     *
     * @param userId identificador do utilizador que executa a operação.
     * @param gameId identificador do jogo ao qual a conquista pertence.
     * @param apiName identificador único externo da conquista.
     * @param name nome da conquista.
     * @param description descrição da conquista.
     * @param icon representação visual (ex: URL ou path).
     *
     * @return [Achievement] criado em caso de sucesso,
     * ou um erro do tipo [AchievementError].
     */
    fun createAchievement(
        userId: Int,
        gameId: Int,
        apiName: String,
        name: String,
        description: String,
        icon: String,
    ): Either<AchievementError, Achievement> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(AchievementError.UserNotAdmin)
            repoGames.findById(gameId) ?: return@run failure(AchievementError.GameNotFound)
            val existingAchievement = repoAchievements.findByApiName(apiName)
            if (existingAchievement != null) return@run failure(AchievementError.AchievementAlreadyExists)
            val achievement =
                repoAchievements.createAchievement(
                    apiName = apiName,
                    name = name,
                    icon = icon,
                    description = description,
                    gameId = gameId,
                )
            success(achievement)
        }

    /**
     * Obtém todas as conquistas associadas a um determinado jogo.
     *
     * @param gameId identificador do jogo.
     *
     * @return lista de [Achievement] associadas ao jogo.
     */
    fun findByGameId(gameId: Int): List<Achievement> =
        trxManager.run {
            repoAchievements.findByGameId(gameId)
        }

    /**
     * Remove todas as conquistas associadas a um jogo.
     *
     * Regras de validação:
     * - o utilizador tem de existir e ser administrador;
     * - o jogo tem de existir.
     *
     * @param userId identificador do utilizador que executa a operação.
     * @param gameId identificador do jogo cujas conquistas serão removidas.
     *
     * @return `true` em caso de sucesso,
     * ou um erro do tipo [AchievementError].
     */
    fun removeAchievements(
        userId: Int,
        gameId: Int,
    ): Either<AchievementError, Boolean> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(AchievementError.UserNotAdmin)
            repoGames.findById(gameId) ?: return@run failure(AchievementError.GameNotFound)
            repoAchievements.removeAchievements(gameId)
            success(true)
        }
}
