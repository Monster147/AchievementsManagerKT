package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.interfaces.Transaction

class TransactionInJdbi(
    private val handle: Handle,
) : Transaction {
    override val repoUsers = RepositoryUserJdbi(handle)
    override val repoGames = RepositoryGameJdbi(handle)
    override val repoUserGames = RepositoryUserGamesJdbi(handle)
    override val repoAchievements = RepositoryAchievementsJdbi(handle)
    override val repoGameProgress = RepositoryGameProgressJdbi(handle)

    override fun rollback() {
        handle.rollback()
    }
}
