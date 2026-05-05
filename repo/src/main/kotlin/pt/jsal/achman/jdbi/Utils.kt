package pt.jsal.achman.jdbi

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.jsal.achman.jdbi.mapper.GameGenreListMapper
import pt.jsal.achman.jdbi.mapper.IntListMapper
import pt.jsal.achman.jdbi.mapper.PasswordValidationInfoMapper
import pt.jsal.achman.jdbi.mapper.TokenValidationInfoMapper
import pt.jsal.achman.token.TokenValidationInfo
import pt.jsal.achman.user.PasswordValidationInfo

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())
    registerColumnMapper(PasswordValidationInfo::class.java,
        PasswordValidationInfoMapper()
    )
    registerColumnMapper(TokenValidationInfo::class.java,
        TokenValidationInfoMapper()
    )
    registerColumnMapper(IntListMapper())
    registerColumnMapper(GameGenreListMapper())

    return this
}
