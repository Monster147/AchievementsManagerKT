package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.game.Game
import pt.achman.game.GameGenre
import pt.achman.game.GamePlatform
import pt.achman.game.GameSource
import pt.achman.interfaces.RepositoryGame
import java.sql.ResultSet

class RepositoryGameJdbi(
    private val handle: Handle,
) : RepositoryGame {
    override fun createGame(
        externalGameId: String,
        name: String,
        source: GameSource,
    ): Game {
        val id =
            handle.createUpdate(
                """
                INSERT INTO dbo.games(external_game_id, name, source)
                VALUES (:externalGameId, :name, :source)
                RETURNING id
                """.trimIndent(),
            )
                .bind("externalGameId", externalGameId)
                .bind("name", name)
                .bind("source", source.name)
                .executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java)
                .one()

        return Game(
            id = id,
            externalGameId = externalGameId,
            name = name,
            source = source,
        )
    }

    override fun findByExternalId(
        externalGameId: String,
        source: GameSource,
    ): Game? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.games
            WHERE external_game_id = :externalGameId AND source = :source
            """.trimIndent(),
        )
            .bind("externalGameId", externalGameId)
            .bind("source", source.name)
            .map { rs, _ -> mapRowToGame(rs) }
            .findFirst()
            .orElse(null)
    }

    override fun findById(id: Int): Game? {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.games
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .map { rs, _ -> mapRowToGame(rs) }
            .findFirst()
            .orElse(null)
    }

    override fun findAll(): List<Game> {
        return handle.createQuery(
            """
            SELECT *
            FROM dbo.games
            """.trimIndent(),
        )
            .map { rs, _ -> mapRowToGame(rs) }
            .toList()
    }

    override fun save(entity: Game) {
        val genreArray =
            handle.connection.createArrayOf(
                "varchar",
                entity.genre.map { it.name }.toTypedArray(),
            )
        handle.createUpdate(
            """
            UPDATE dbo.games
            SET external_game_id = :externalGameId,
                name = :name,
                genre = :genre,
                platform = :platform,
                release_year = :releaseYear,
                source = :source,
                cover = :cover
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", entity.id)
            .bind("externalGameId", entity.externalGameId)
            .bind("name", entity.name)
            .bindBySqlType("genre", genreArray, java.sql.Types.ARRAY)
            .bind("platform", entity.platform.name)
            .bind("releaseYear", entity.releaseYear)
            .bind("source", entity.source.name)
            .bind("cover", entity.cover)
            .execute()
    }

    override fun deleteById(id: Int) {
        handle.createUpdate("DELETE FROM dbo.games WHERE id = :id")
            .bind("id", id)
            .execute()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.games").execute()
    }

    private fun mapRowToGame(rs: ResultSet): Game {
        val genres =
            rs.getArray("genre")?.let { arr ->
                (arr.array as Array<*>).map { GameGenre.valueOf(it.toString()) }
            } ?: emptyList()
        return Game(
            id = rs.getInt("id"),
            externalGameId = rs.getString("external_game_id"),
            name = rs.getString("name"),
            genre = genres,
            platform = GamePlatform.valueOf(rs.getString("platform")),
            releaseYear = rs.getString("release_year"),
            source = GameSource.valueOf(rs.getString("source")),
            cover = rs.getString("cover") ?: "",
        )
    }
}
