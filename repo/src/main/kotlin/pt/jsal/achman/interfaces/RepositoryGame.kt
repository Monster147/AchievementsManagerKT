package pt.jsal.achman.interfaces

import pt.jsal.achman.game.Game
import pt.jsal.achman.game.GameGenre
import pt.jsal.achman.game.GamePlatform
import pt.jsal.achman.game.GameSource

/**
 * Repositório de operações sobre jogos.
 */
interface RepositoryGame : pt.jsal.achman.interfaces.Repository<Game> {
    /**
     * Cria um jogo no sistema a partir de um identificador externo.
     *
     * @param externalGameId Identificador do jogo na fonte externa.
     * @param name Nome do jogo.
     * @param source Fonte de onde o jogo foi obtido.
     *
     * @return [Game] criado.
     */
    fun createGame(
        externalGameId: String,
        name: String,
        source: GameSource,
    ): Game

    /**
     * Procura um jogo com base no identificador externo e na fonte.
     *
     * @param externalGameId Identificador do jogo na fonte externa.
     * @param source Fonte do jogo.
     *
     * @return [Game] correspondente, ou `null` caso não exista.
     */
    fun findByExternalId(
        externalGameId: String,
        source: GameSource,
    ): Game?

    /**
     * Atualiza parcialmente a informação de um jogo existente.
     *
     * Apenas os campos não nulos serão atualizados.
     *
     * @param game Jogo a atualizar.
     * @param externalGameId Novo identificador externo (opcional).
     * @param name Novo nome do jogo (opcional).
     * @param genres Lista de géneros do jogo (opcional).
     * @param platform Plataforma do jogo (opcional).
     * @param releaseYear Ano de lançamento (opcional).
     * @param source Nova fonte do jogo (opcional).
     * @param cover URL ou referência da imagem de capa (opcional).
     *
     * @return [Game] atualizado.
     */
    fun updateGameInfo(
        game: Game,
        externalGameId: String?,
        name: String?,
        genres: List<GameGenre>?,
        platform: GamePlatform?,
        releaseYear: String?,
        source: GameSource?,
        cover: String?,
    ): Game
}
