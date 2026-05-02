package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.achievement.Achievement
import pt.achman.interfaces.RepositoryAchievements
import java.sql.ResultSet

class RepositoryAchievementsJdbi(
    private val handle: Handle,
) : RepositoryAchievements {
    override fun createAchievement(
        apiName: String,
        name: String,
        icon: String,
        description: String,
        gameId: Int,
    ): Achievement {
        val id =
            handle.createUpdate(
                """
                INSERT INTO dbo.achievements(api_name, name, icon, description, game_id)
                VALUES (:apiName, :name, :icon, :description, :gameId)
                RETURNING id
                """.trimIndent(),
            )
                .bind("apiName", apiName)
                .bind("name", name)
                .bind("icon", icon)
                .bind("description", description)
                .bind("gameId", gameId)
                .executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java)
                .one()
        return Achievement(
            id = id,
            apiName = apiName,
            name = name,
            icon = icon,
            description = description,
            gameId = gameId,
        )
    }

    override fun findByGameId(gameId: Int): List<Achievement> {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.achievements
            WHERE game_id = :gameId
            """.trimIndent(),
        )
            .bind("gameId", gameId)
            .map { rs, _ -> mapRowToAchievement(rs) }
            .toList()
    }

    override fun findByApiName(apiName: String): Achievement? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.achievements
            WHERE api_name = :apiName
            """.trimIndent(),
        )
            .bind("apiName", apiName)
            .map { rs, _ -> mapRowToAchievement(rs) }
            .singleOrNull()
    }

    override fun findById(id: Int): Achievement? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.achievements
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .map { rs, _ -> mapRowToAchievement(rs) }
            .singleOrNull()
    }

    override fun findAll(): List<Achievement> {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.achievements
            """.trimIndent(),
        )
            .map { rs, _ -> mapRowToAchievement(rs) }
            .toList()
    }

    override fun save(entity: Achievement) {
        handle.createUpdate(
            """
            UPDATE dbo.achievements
            SET api_name = :apiName,
                name = :name,
                icon = :icon,
                description = :description,
                game_id = :gameId
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", entity.id)
            .bind("apiName", entity.apiName)
            .bind("name", entity.name)
            .bind("icon", entity.icon)
            .bind("description", entity.description)
            .bind("gameId", entity.gameId)
            .execute()
    }

    override fun deleteById(id: Int) {
        handle.createUpdate(
            """
            DELETE FROM dbo.achievements
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .execute()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.achievements").execute()
    }

    private fun mapRowToAchievement(rs: ResultSet): Achievement =
        Achievement(
            id = rs.getInt("id"),
            apiName = rs.getString("api_name"),
            name = rs.getString("name"),
            icon = rs.getString("icon"),
            description = rs.getString("description"),
            gameId = rs.getInt("game_id"),
        )
}
