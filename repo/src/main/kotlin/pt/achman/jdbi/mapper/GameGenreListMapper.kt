package pt.achman.jdbi.mapper

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.achman.game.GameGenre
import java.sql.ResultSet
import java.sql.SQLException

class GameGenreListMapper : ColumnMapper<List<GameGenre>> {
    @Throws(SQLException::class)
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): List<GameGenre> {
        val sqlArray = rs.getArray(columnNumber) ?: return emptyList()
        val javaArray = sqlArray.array as Array<*>
        return javaArray.mapNotNull { it?.toString() }.map { GameGenre.valueOf(it) }
    }
}
