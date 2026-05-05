package pt.jsal.achman

import org.springframework.stereotype.Component
import pt.jsal.achman.achievement.GameProgress
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success

/**
 * Representa os possíveis erros associados às operações sobre progresso de jogo.
 */
sealed class GameProgressError {
    /**
     * Indica que o jogo especificado não foi encontrado.
     */
    data object GameNotFound : GameProgressError()

    /**
     * Indica que a conquista especificada não foi encontrada.
     */
    data object AchievementNotFound : GameProgressError()

    /**
     * Indica que o utilizador especificado não foi encontrado.
     */
    data object UserNotFound : GameProgressError()

    /**
     * Indica que não existe progresso registado para o utilizador e jogo.
     */
    data object ProgressNotFound : GameProgressError()
}

/**
 * Serviço responsável pela gestão do progresso de jogos dos utilizadores.
 *
 * Responsabilidades principais:
 * - criação de progresso associado a um utilizador e jogo;
 * - consulta de progresso por utilizador e jogo;
 * - gestão de conquistas completadas;
 * - limpeza de conquistas associadas ao progresso;
 * - validação de existência de entidades (utilizadores, jogos, conquistas).
 *
 * Todas as operações são executadas dentro de uma transação, através do [TransactionManager].
 *
 * @param trxManager gestor de transações usado para aceder aos repositórios dentro de unidades de trabalho.
 */
@Component
class GameProgressService(
    private val trxManager: TransactionManager,
) {
    /**
     * Cria um registo de progresso para um utilizador num determinado jogo.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     *
     * @return [GameProgress] criado em caso de sucesso,
     * ou um erro do tipo [GameProgressError].
     */
    fun createGameProgress(
        userId: Int,
        gameId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            val progress = repoGameProgress.createGameProgress(userId, gameId)
            success(progress)
        }

    /**
     * Obtém o progresso de um utilizador num jogo específico.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     *
     * @return [GameProgress] correspondente, ou erro do tipo [GameProgressError].
     */
    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            val progress = repoGameProgress.findByUserIdAndGameId(userId, gameId)
            if (progress == null) return@run failure(GameProgressError.ProgressNotFound)
            success(progress)
        }

    /**
     * Obtém todos os registos de progresso de um utilizador.
     *
     * @param userId identificador do utilizador.
     *
     * @return lista de [GameProgress] associados ao utilizador.
     */
    fun findByUserId(userId: Int): List<GameProgress> =
        trxManager.run {
            repoGameProgress.findByUserId(userId)
        }

    /**
     * Adiciona uma conquista completada ao progresso de um utilizador.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     * @param achievementId identificador da conquista.
     *
     * @return [GameProgress] atualizado, ou erro do tipo [GameProgressError].
     */
    fun addCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            repoAchievements.findById(achievementId) ?: return@run failure(GameProgressError.AchievementNotFound)
            val progress = repoGameProgress.addCompletedAchievement(userId, gameId, achievementId)
            success(progress)
        }

    /**
     * Remove uma conquista completada do progresso de um utilizador.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     * @param achievementId identificador da conquista.
     *
     * @return [GameProgress] atualizado, ou erro do tipo [GameProgressError].
     */
    fun removeCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            repoAchievements.findById(achievementId) ?: return@run failure(GameProgressError.AchievementNotFound)
            val progress = repoGameProgress.removeCompletedAchievement(userId, gameId, achievementId)
            success(progress)
        }

    /**
     * Remove todas as conquistas completadas de um progresso.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     *
     * @return [GameProgress] atualizado, ou erro do tipo [GameProgressError].
     */
    fun clearCompletedAchievements(
        userId: Int,
        gameId: Int,
    ): Either<GameProgressError, GameProgress> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(GameProgressError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(GameProgressError.GameNotFound)
            val progress =
                repoGameProgress.clearCompletedAchievements(userId, gameId) ?: return@run failure(
                    GameProgressError.ProgressNotFound,
                )
            success(progress)
        }
}
