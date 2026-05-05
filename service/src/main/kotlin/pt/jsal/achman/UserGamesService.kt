package pt.jsal.achman

import org.springframework.stereotype.Component
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.usergame.UserGame
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success

/**
 * Representa os possíveis erros associados às operações sobre a relação utilizador-jogo.
 */
sealed class UserGamesError {

    /**
     * Indica que já existe uma associação entre o utilizador e o jogo.
     */
    data object UserGameAlreadyExists : UserGamesError()

    /**
     * Indica que a associação entre o utilizador e o jogo não foi encontrada.
     */
    data object UserGameNotFound : UserGamesError()

    /**
     * Indica que o utilizador especificado não foi encontrado.
     */
    data object UserNotFound : UserGamesError()
}

/**
 * Serviço responsável pela gestão da relação entre utilizadores e jogos.
 *
 * Responsabilidades principais:
 * - criação da associação entre utilizador e jogo;
 * - consulta de jogos associados a um utilizador;
 * - consulta de uma associação específica utilizador-jogo;
 * - alteração de opções associadas à relação (ex: sincronização).
 *
 * Todas as operações são executadas dentro de uma transação, através do [TransactionManager].
 *
 * @param trxManager gestor de transações usado para aceder aos repositórios dentro de unidades de trabalho.
 */
@Component
class UserGamesService(
    private val trxManager: TransactionManager,
) {
    /**
     * Cria uma associação entre um utilizador e um jogo.
     *
     * Regras de validação:
     * - o utilizador tem de existir;
     * - o jogo tem de existir;
     * - não pode existir já uma associação entre o utilizador e o jogo.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     *
     * @return [UserGame] criado em caso de sucesso,
     * ou um erro do tipo [UserGamesError].
     */
    fun createUserGame(
        userId: Int,
        gameId: Int,
    ): Either<UserGamesError, UserGame> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(UserGamesError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(UserGamesError.UserGameNotFound)
            val existing = repoUserGames.findByUserIdAndGameId(userId, gameId)
            if (existing != null) return@run failure(UserGamesError.UserGameAlreadyExists)
            val userGame = repoUserGames.createUserGame(userId, gameId, synchronize = false)
            success(userGame)
        }

    /**
     * Obtém todas as associações de jogos de um utilizador.
     *
     * @param userId identificador do utilizador.
     *
     * @return lista de [UserGame] associados ao utilizador.
     */
    fun findByUserId(userId: Int): List<UserGame> =
        trxManager.run {
            repoUserGames.findByUserId(userId)
        }

    /**
     * Obtém a associação entre um utilizador e um jogo específico.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     *
     * @return [UserGame] correspondente, ou erro do tipo [UserGamesError].
     */
    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): Either<UserGamesError, UserGame> =
        trxManager.run {
            val userGame = repoUserGames.findByUserIdAndGameId(userId, gameId)
            if (userGame == null) {
                failure(UserGamesError.UserGameNotFound)
            } else {
                success(userGame)
            }
        }

    /**
     * Altera a opção de sincronização de uma associação utilizador-jogo.
     *
     * Regras de validação:
     * - o utilizador tem de existir;
     * - o jogo tem de existir;
     * - a associação utilizador-jogo tem de existir.
     *
     * @param userId identificador do utilizador.
     * @param gameId identificador do jogo.
     *
     * @return [UserGame] atualizado, ou erro do tipo [UserGamesError].
     */
    fun alterSyncOption(
        userId: Int,
        gameId: Int,
    ): Either<UserGamesError, UserGame> =
        trxManager.run {
            repoUsers.findById(userId) ?: return@run failure(UserGamesError.UserNotFound)
            repoGames.findById(gameId) ?: return@run failure(UserGamesError.UserGameNotFound)
            val userGame = repoUserGames.findByUserIdAndGameId(userId, gameId)
            if (userGame == null) {
                failure(UserGamesError.UserGameNotFound)
            } else {
                val userGameUpdated = repoUserGames.alterSyncOption(userGame)
                success(userGameUpdated)
            }
        }
}
