package pt.achman.interfaces

/**
 * O ciclo de vida de uma Transaction é gerido fora do âmbito do contentor de IoC/DI.
 * As transações são instanciadas por um TransactionManager,
 * gerido pelo contentor de IoC/DI (ex: Spring).
 * A implementação de Transaction é responsável por criar as
 * instâncias necessárias de repositórios no seu construtor.
 */
interface Transaction {
    val repoUsers: RepositoryUser
    val repoGames: RepositoryGame
    val repoUserGames: RepositoryUserGames
    val repoAchievements: RepositoryAchievements
    val repoGameProgress: RepositoryGameProgress

    fun rollback()
}
