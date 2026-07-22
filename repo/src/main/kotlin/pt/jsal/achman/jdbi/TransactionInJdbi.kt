package pt.jsal.achman.jdbi

import com.fasterxml.jackson.databind.ObjectMapper
import org.jdbi.v3.core.Handle
import pt.jsal.achman.common.RepositoryIntegrationsConfigFile
import pt.jsal.achman.interfaces.Transaction

class TransactionInJdbi(
    private val handle: Handle,
    private val objectMapper: ObjectMapper,
) : Transaction {
    override val repoUsers = RepositoryUserJdbi(handle)
    override val repoGames = RepositoryGameJdbi(handle)
    override val repoUserGames = RepositoryUserGamesJdbi(handle)
    override val repoAchievements = RepositoryAchievementsJdbi(handle)
    override val repoGameProgress = RepositoryGameProgressJdbi(handle)
    override val repoConfig = RepositoryIntegrationsConfigFile(objectMapper)

    override fun rollback() {
        handle.rollback()
    }
}
