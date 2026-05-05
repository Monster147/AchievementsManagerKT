package pt.jsal.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.jsal.achman.interfaces.RepositoryUserGames
import pt.jsal.achman.usergame.UserGame
import java.sql.ResultSet

class RepositoryUserGamesJdbi(
    private val handle: Handle,
) : RepositoryUserGames {
    override fun createUserGame(
        userId: Int,
        gameId: Int,
        synchronize: Boolean,
    ): UserGame {
        val id =
            handle.createUpdate(
                """
                INSERT INTO dbo.user_games(user_id, game_id, synchronize)
                VALUES (:userId, :gameId, :synchronize)
                RETURNING id
                """.trimIndent(),
            )
                .bind("userId", userId)
                .bind("gameId", gameId)
                .bind("synchronize", synchronize)
                .executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java)
                .one()

        return UserGame(
            id = id,
            userId = userId,
            gameId = gameId,
            synchronize = synchronize,
        )
    }

    override fun findByUserId(userId: Int): List<UserGame> {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.user_games
            WHERE user_id = :userId
            """.trimIndent(),
        )
            .bind("userId", userId)
            .map { rs, _ -> mapRowToUserGame(rs) }
            .toList()
    }

    override fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): UserGame? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.user_games
            WHERE user_id = :userId AND game_id = :gameId
            """.trimIndent(),
        )
            .bind("userId", userId)
            .bind("gameId", gameId)
            .map { rs, _ -> mapRowToUserGame(rs) }
            .singleOrNull()
    }

    override fun alterSyncOption(userGame: UserGame): UserGame {
        val updatedUserGame = userGame.copy(synchronize = !userGame.synchronize)
        save(updatedUserGame)
        return updatedUserGame
    }

    override fun findById(id: Int): UserGame? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.user_games
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .map { rs, _ -> mapRowToUserGame(rs) }
            .singleOrNull()
    }

    override fun findAll(): List<UserGame> {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.user_games
            """.trimIndent(),
        )
            .map { rs, _ -> mapRowToUserGame(rs) }
            .toList()
    }

    override fun save(entity: UserGame) {
        handle.createUpdate(
            """
            UPDATE dbo.user_games
            SET user_id = :userId, game_id = :gameId, synchronize = :synchronize
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", entity.id)
            .bind("userId", entity.userId)
            .bind("gameId", entity.gameId)
            .bind("synchronize", entity.synchronize)
            .execute()
    }

    override fun deleteById(id: Int) {
        handle.createUpdate(
            """
            DELETE FROM dbo.user_games
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .execute()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.user_games").execute()
    }

    private fun mapRowToUserGame(rs: ResultSet): UserGame {
        return UserGame(
            id = rs.getInt("id"),
            userId = rs.getInt("user_id"),
            gameId = rs.getInt("game_id"),
            synchronize = rs.getBoolean("synchronize"),
        )
    }
}
