package pt.jsal.achman.mem

import com.fasterxml.jackson.databind.ObjectMapper
import pt.jsal.achman.common.RepositoryIntegrationsConfigFile
import pt.jsal.achman.interfaces.Transaction
import pt.jsal.achman.interfaces.TransactionManager

class TransactionManagerInMem(
    private val objectMapper: ObjectMapper,
) : TransactionManager {
    private val repoUsers = RepositoryUserMem()
    private val repoGames = RepositoryGameMem()
    private val repoUserGames = RepositoryUserGamesMem()
    private val repoAchievements = RepositoryAchievementsMem()
    private val repoGameProgress = RepositoryGameProgressMem()

    private val repoConfig = RepositoryIntegrationsConfigFile(objectMapper)

    override fun <R> run(block: Transaction.() -> R): R =
        block(
            TransactionInMem(
                repoUsers,
                repoGames,
                repoUserGames,
                repoAchievements,
                repoGameProgress,
                repoConfig,
            ),
        )
}
