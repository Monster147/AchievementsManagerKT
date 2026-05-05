package pt.jsal.achman.interfaces

/**
 * O ciclo de vida de uma Transaction é gerido fora do âmbito do contentor de IoC/DI.
 * As transações são instanciadas por um TransactionManager,
 * gerido pelo contentor de IoC/DI (ex: Spring).
 * A implementação de Transaction é responsável por criar as
 * instâncias necessárias de repositórios no seu construtor.
 */
interface Transaction {
    val repoUsers: pt.jsal.achman.interfaces.RepositoryUser
    val repoGames: pt.jsal.achman.interfaces.RepositoryGame
    val repoUserGames: pt.jsal.achman.interfaces.RepositoryUserGames
    val repoAchievements: pt.jsal.achman.interfaces.RepositoryAchievements
    val repoGameProgress: pt.jsal.achman.interfaces.RepositoryGameProgress

    fun rollback()
}
