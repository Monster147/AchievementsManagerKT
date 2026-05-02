package pt.achman.mem

import pt.achman.interfaces.RepositoryAchievements
import pt.achman.interfaces.RepositoryGame
import pt.achman.interfaces.RepositoryGameProgress
import pt.achman.interfaces.RepositoryUser
import pt.achman.interfaces.RepositoryUserGames
import pt.achman.interfaces.Transaction

class TransactionInMem(
    override val repoUsers: RepositoryUser,
    override val repoGames: RepositoryGame,
    override val repoUserGames: RepositoryUserGames,
    override val repoAchievements: RepositoryAchievements,
    override val repoGameProgress: RepositoryGameProgress,
) : Transaction {
    override fun rollback(): Unit = throw UnsupportedOperationException()
}
