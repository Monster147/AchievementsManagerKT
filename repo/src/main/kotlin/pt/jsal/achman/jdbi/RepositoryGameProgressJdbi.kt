package pt.jsal.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.jsal.achman.achievement.GameProgress
import pt.jsal.achman.interfaces.RepositoryGameProgress
import java.sql.ResultSet

class RepositoryGameProgressJdbi(
    private val handle: Handle,
) : RepositoryGameProgress {
    override fun createGameProgress(
        userId: Int,
        gameId: Int,
    ): GameProgress {
        val id =
            handle.createUpdate(
                """
                INSERT INTO dbo.game_progress(user_id, game_id, completed_achievements)
                VALUES (:userId, :gameId, :completedAchievements)
                RETURNING id
                """.trimIndent(),
            )
                .bind("userId", userId)
                .bind("gameId", gameId)
                .bind("completedAchievements", emptyArray<Int>())
                .executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java)
                .one()

        return GameProgress(
            id = id,
            userId = userId,
            gameId = gameId,
            completedAchievements = emptyList(),
        )
    }

    override fun findByUserIdAndGameId(
        userId: Int,
        gameId: Int,
    ): GameProgress? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.game_progress
            WHERE user_id = :userId AND game_id = :gameId
            """.trimIndent(),
        )
            .bind("userId", userId)
            .bind("gameId", gameId)
            .map { rs, _ -> mapRowToGameProgress(rs) }
            .singleOrNull()
    }

    override fun findByUserId(userId: Int): List<GameProgress> {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.game_progress
            WHERE user_id = :userId
            """.trimIndent(),
        )
            .bind("userId", userId)
            .map { rs, _ -> mapRowToGameProgress(rs) }
            .toList()
    }

    override fun addCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress {
        val progress = findByUserIdAndGameId(userId, gameId) ?: createGameProgress(userId, gameId)
        if (!progress.completedAchievements.contains(achievementId)) {
            val updated =
                progress.copy(
                    completedAchievements = progress.completedAchievements + achievementId,
                )
            save(updated)
            return updated
        }
        return progress
    }

    override fun removeCompletedAchievement(
        userId: Int,
        gameId: Int,
        achievementId: Int,
    ): GameProgress {
        val progress = findByUserIdAndGameId(userId, gameId) ?: createGameProgress(userId, gameId)
        if (progress.completedAchievements.contains(achievementId)) {
            val updated =
                progress.copy(
                    completedAchievements = progress.completedAchievements - achievementId,
                )
            save(updated)
            return updated
        }
        return progress
    }

    override fun clearCompletedAchievements(
        userId: Int,
        gameId: Int,
    ): GameProgress? {
        val progress = findByUserIdAndGameId(userId, gameId) ?: return null
        val updated =
            progress.copy(
                completedAchievements = emptyList(),
            )
        save(updated)
        return updated
    }

    override fun removeUserProgress(userId: Int) {
        handle.createUpdate(
            """
            DELETE FROM dbo.game_progress
            WHERE user_id = :userId
            """.trimIndent(),
        )
            .bind("userId", userId)
            .execute()
    }

    override fun findById(id: Int): GameProgress? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.game_progress
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .map { rs, _ -> mapRowToGameProgress(rs) }
            .singleOrNull()
    }

    override fun findAll(): List<GameProgress> {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.game_progress
            """.trimIndent(),
        )
            .map { rs, _ -> mapRowToGameProgress(rs) }
            .toList()
    }

    override fun save(entity: GameProgress) {
        handle.createUpdate(
            """
            UPDATE dbo.game_progress
            SET user_id = :userId, game_id = :gameId, completed_achievements = :completedAchievements
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", entity.id)
            .bind("userId", entity.userId)
            .bind("gameId", entity.gameId)
            .bind("completedAchievements", entity.completedAchievements.toTypedArray())
            .execute()
    }

    override fun deleteById(id: Int) {
        handle.createUpdate(
            """
            DELETE FROM dbo.game_progress
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .execute()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.game_progress").execute()
    }

    private fun mapRowToGameProgress(rs: ResultSet): GameProgress {
        val completedAchievements =
            rs.getArray("completed_achievements")?.let { arr ->
                (arr.array as Array<*>).map { (it as Number).toInt() }
            } ?: emptyList()
        return GameProgress(
            id = rs.getInt("id"),
            gameId = rs.getInt("game_id"),
            userId = rs.getInt("user_id"),
            completedAchievements = completedAchievements,
        )
    }
}
