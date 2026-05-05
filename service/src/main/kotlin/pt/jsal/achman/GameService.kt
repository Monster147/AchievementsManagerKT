package pt.jsal.achman

import org.springframework.stereotype.Component
import pt.jsal.achman.game.Game
import pt.jsal.achman.game.GameGenre
import pt.jsal.achman.game.GamePlatform
import pt.jsal.achman.game.GameSource
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.user.UserRole
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success


/**
 * Representa os possíveis erros associados às operações sobre jogos.
 */
sealed class GameError {

    /**
     * Indica que já existe um jogo com o mesmo identificador externo e origem.
     */
    data object GameAlreadyExists : GameError()

    /**
     * Indica que o jogo especificado não foi encontrado.
     */
    data object GameNotFound : GameError()

    /**
     * Indica que o utilizador não tem permissões de administrador.
     */
    data object UserNotAdmin : GameError()
}

/**
 * Serviço responsável pela gestão de jogos.
 *
 * Responsabilidades principais:
 * - criação de jogos com base em identificadores externos;
 * - consulta de jogos por identificador interno ou externo;
 * - atualização de informação de jogos;
 * - remoção de jogos;
 * - validação de permissões de utilizador (admin).
 *
 * Todas as operações são executadas dentro de uma transação, através do [TransactionManager].
 *
 * @param trxManager gestor de transações usado para aceder aos repositórios dentro de unidades de trabalho.
 */
@Component
class GameService(
    private val trxManager: TransactionManager,
) {

    /**
     * Cria um jogo.
     *
     * Regras de validação:
     * - o utilizador tem de existir e ser administrador;
     * - não pode existir outro jogo com o mesmo `externalGameId` e `source`.
     *
     * @param userId identificador do utilizador que executa a operação.
     * @param externalGameId identificador externo do jogo (ex: API externa).
     * @param name nome do jogo.
     * @param source origem do jogo.
     *
     * @return [Game] criado em caso de sucesso,
     * ou um erro do tipo [GameError].
     */
    fun createGame(
        userId: Int,
        externalGameId: String,
        name: String,
        source: GameSource,
    ): Either<GameError, Game> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(GameError.UserNotAdmin)
            val existingGame = repoGames.findByExternalId(externalGameId, source)
            if (existingGame != null) return@run failure(GameError.GameAlreadyExists)
            val newGame = repoGames.createGame(externalGameId, name, source)
            success(newGame)
        }

    /**
     * Obtém um jogo com base no seu identificador externo e origem.
     *
     * @param externalGameId identificador externo do jogo.
     * @param source origem do jogo.
     *
     * @return [Game] correspondente, ou erro do tipo [GameError].
     */
    fun findByExternalId(
        externalGameId: String,
        source: GameSource,
    ): Either<GameError, Game> =
        trxManager.run {
            val game = repoGames.findByExternalId(externalGameId, source)
            if (game == null) {
                failure(GameError.GameNotFound)
            } else {
                success(game)
            }
        }

    /**
     * Atualiza a informação de um jogo.
     *
     * Apenas os campos não nulos serão atualizados.
     *
     * Regras de validação:
     * - o utilizador tem de existir e ser administrador;
     * - o jogo tem de existir.
     *
     * @param userId identificador do utilizador que executa a operação.
     * @param gameId identificador do jogo.
     * @param externalGameId novo identificador externo (opcional).
     * @param name novo nome (opcional).
     * @param genres novos géneros (opcional).
     * @param platform nova plataforma (opcional).
     * @param releaseYear novo ano de lançamento (opcional).
     * @param source nova origem (opcional).
     * @param cover nova imagem de capa (opcional).
     *
     * @return [Game] atualizado, ou erro do tipo [GameError].
     */
    fun updateGameInfo(
        userId: Int,
        gameId: Int,
        externalGameId: String?,
        name: String?,
        genres: List<GameGenre>?,
        platform: GamePlatform?,
        releaseYear: String?,
        source: GameSource?,
        cover: String?,
    ): Either<GameError, Game> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(GameError.UserNotAdmin)
            val game = repoGames.findById(gameId) ?: return@run failure(GameError.GameNotFound)
            val updatedGame =
                repoGames.updateGameInfo(
                    game,
                    externalGameId,
                    name,
                    genres,
                    platform,
                    releaseYear,
                    source,
                    cover,
                )
            success(updatedGame)
        }

    /**
     * Obtém um jogo com base no seu identificador interno.
     *
     * @param gameId identificador do jogo.
     *
     * @return [Game] correspondente, ou erro do tipo [GameError].
     */
    fun findById(gameId: Int): Either<GameError, Game> =
        trxManager.run {
            val game = repoGames.findById(gameId)
            if (game == null) {
                return@run failure(GameError.GameNotFound)
            } else {
                return@run success(game)
            }
        }

    /**
     * Obtém todos os jogos existentes.
     *
     * @return lista de [Game].
     */
    fun findAll(): List<Game> = trxManager.run { repoGames.findAll() }

    /**
     * Remove um jogo com base no seu identificador.
     *
     * Regras de validação:
     * - o utilizador tem de existir e ser administrador;
     * - o jogo tem de existir.
     *
     * @param userId identificador do utilizador que executa a operação.
     * @param gameId identificador do jogo.
     *
     * @return `true` em caso de sucesso,
     * ou erro do tipo [GameError].
     */
    fun deleteById(
        userId: Int,
        gameId: Int,
    ): Either<GameError, Boolean> =
        trxManager.run {
            val user = repoUsers.findById(userId)
            if (user == null || user.role != UserRole.ADMIN) return@run failure(GameError.UserNotAdmin)
            repoGames.findById(gameId) ?: return@run failure(GameError.GameNotFound)
            repoGames.deleteById(gameId)
            success(true)
        }
}
