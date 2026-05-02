package pt.achman.interfaces

import pt.achman.usergame.UserGame

interface RepositoryUserGames : Repository<UserGame> {
    fun createUserGame(
        userId: Int,
        gameId: Int,
        synchronize: Boolean,
    ): UserGame

    fun findByUserId(userId: Int): List<UserGame>

    fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): UserGame?
}
