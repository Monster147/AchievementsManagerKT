package pt.achman.jdbi

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.achman.jdbi.mapper.IntListMapper
import pt.achman.jdbi.mapper.PasswordValidationInfoMapper
import pt.achman.jdbi.mapper.TokenValidationInfoMapper
import pt.achman.token.TokenValidationInfo
import pt.achman.user.PasswordValidationInfo

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())
    registerColumnMapper(PasswordValidationInfo::class.java, PasswordValidationInfoMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())
    registerColumnMapper(IntListMapper())

    return this
}
