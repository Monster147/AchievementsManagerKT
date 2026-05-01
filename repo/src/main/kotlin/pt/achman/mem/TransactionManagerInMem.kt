package pt.achman.mem

import pt.achman.interfaces.Transaction
import pt.achman.interfaces.TransactionManager

class TransactionManagerInMem : TransactionManager {
    private val repoUsers = RepositoryUserMem()
    private val repoGames = RepositoryGameMem()
    private val repoUserGames = RepositoryUserGamesMem()
    private val repoAchievements = RepositoryAchievementsMem()
    private val repoGameProgress = RepositoryGameProgressMem()

    override fun <R> run(block: Transaction.() -> R): R =
        block(
            TransactionInMem(
                repoUsers,
                repoGames,
                repoUserGames,
                repoAchievements,
                repoGameProgress,
            )
        )
}
