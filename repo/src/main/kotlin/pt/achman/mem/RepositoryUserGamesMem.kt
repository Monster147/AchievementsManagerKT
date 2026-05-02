package pt.achman.mem

import pt.achman.interfaces.RepositoryUserGames
import pt.achman.usergame.UserGame

class RepositoryUserGamesMem : RepositoryUserGames {
    private val userGames = mutableListOf<UserGame>()

    override fun createUserGame(
        userId: Int,
        gameId: Int,
        synchronize: Boolean,
    ): UserGame =
        UserGame(
            id = userGames.size + 1,
            userId = userId,
            gameId = gameId,
            synchronize = synchronize,
        ).also { userGames.add(it) }

    override fun findByUserId(userId: Int): List<UserGame> = userGames.filter { it.userId == userId }

    override fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): UserGame? = userGames.find { it.userId == userId && it.gameId == gameId }

    override fun findById(id: Int): UserGame? = userGames.find { it.id == id }

    override fun findAll(): List<UserGame> = userGames.toList()

    override fun save(entity: UserGame) {
        userGames.removeIf { it.id == entity.id }
        userGames.add(entity)
    }

    override fun deleteById(id: Int) {
        userGames.removeIf { it.id == id }
    }

    override fun clear() {
        userGames.clear()
    }
}
