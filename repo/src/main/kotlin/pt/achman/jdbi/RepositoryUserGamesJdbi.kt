package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.interfaces.RepositoryUserGames
import pt.achman.usergame.UserGame

class RepositoryUserGamesJdbi(
    handle: Handle
): RepositoryUserGames {
    override fun createUserGame(
        userId: Int,
        gameId: Int,
        synchronize: Boolean
    ): UserGame {
        TODO("Not yet implemented")
    }

    override fun findByUserId(userId: Int): List<UserGame> {
        TODO("Not yet implemented")
    }

    override fun findByUserIdAndGameId(userId: Int, gameId: Int): UserGame? {
        TODO("Not yet implemented")
    }

    override fun findById(id: Int): UserGame? {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<UserGame> {
        TODO("Not yet implemented")
    }

    override fun save(entity: UserGame) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Int) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}