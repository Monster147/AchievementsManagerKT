package pt.jsal.achman.mem

import pt.jsal.achman.interfaces.RepositoryAchievements
import pt.jsal.achman.interfaces.RepositoryGame
import pt.jsal.achman.interfaces.RepositoryGameProgress
import pt.jsal.achman.interfaces.RepositoryUser
import pt.jsal.achman.interfaces.RepositoryUserGames
import pt.jsal.achman.interfaces.Transaction

class TransactionInMem(
    override val repoUsers: RepositoryUser,
    override val repoGames: RepositoryGame,
    override val repoUserGames: RepositoryUserGames,
    override val repoAchievements: RepositoryAchievements,
    override val repoGameProgress: RepositoryGameProgress,
) : Transaction {
    override fun rollback(): Unit = throw UnsupportedOperationException()
}
