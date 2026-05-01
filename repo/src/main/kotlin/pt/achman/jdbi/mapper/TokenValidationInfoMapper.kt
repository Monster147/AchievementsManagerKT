package pt.achman.jdbi.mapper

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.achman.token.TokenValidationInfo
import java.sql.ResultSet
import java.sql.SQLException

class TokenValidationInfoMapper : ColumnMapper<TokenValidationInfo> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): TokenValidationInfo = TokenValidationInfo(r.getString(columnNumber))
}
