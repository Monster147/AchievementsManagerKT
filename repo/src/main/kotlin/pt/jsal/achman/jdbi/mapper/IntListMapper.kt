package pt.jsal.achman.jdbi.mapper

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class IntListMapper : ColumnMapper<List<Int>> {
    @Throws(SQLException::class)
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): List<Int> {
        val sqlArray = rs.getArray(columnNumber) ?: return emptyList()
        val javaArray = sqlArray.array as Array<*>
        return javaArray.map { (it as Number).toInt() }
    }
}
