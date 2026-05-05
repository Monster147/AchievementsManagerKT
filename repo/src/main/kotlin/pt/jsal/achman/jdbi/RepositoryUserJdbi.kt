package pt.jsal.achman.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.jsal.achman.interfaces.RepositoryUser
import pt.jsal.achman.token.Token
import pt.jsal.achman.token.TokenValidationInfo
import pt.jsal.achman.user.PasswordValidationInfo
import pt.jsal.achman.user.User
import pt.jsal.achman.user.UserRole
import java.sql.ResultSet
import java.time.Instant

class RepositoryUserJdbi(
    private val handle: Handle,
) : RepositoryUser {
    override fun createUser(
        name: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
        role: UserRole,
    ): User {
        val id =
            handle
                .createUpdate(
                    """
            INSERT INTO dbo.users (name, email, password_validation, role) 
            VALUES (:name, :email, :password_validation, :role)
            RETURNING id
            """,
                ).bind("name", name)
                .bind("email", email)
                .bind("password_validation", passwordValidation.validationInfo)
                .bind("role", role.name)
                .executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java)
                .one()

        return User(id = id, name = name, email = email, passwordValidation = passwordValidation, role = role)
    }

    override fun findByEmail(email: String): User? =
        handle
            .createQuery("SELECT * FROM dbo.users WHERE email = :email")
            .bind("email", email)
            .map {
                    rs, _,
                ->
                mapRow(rs)
            }
            .findOne()
            .orElse(null)

    override fun findByRole(role: UserRole): List<User> =
        handle.createQuery("SELECT * FROM dbo.users WHERE role = :role")
            .bind("role", role.name)
            .map { rs, _ -> mapRow(rs) }
            .toList()

    override fun updateRole(
        user: User,
        role: UserRole,
    ): User {
        val updated = user.copy(role = role)
        save(updated)
        return updated
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? =
        handle
            .createQuery(
                """
                SELECT users.id AS id,
                       users.name AS name,
                       users.email AS email,
                       users.password_validation AS password_validation,
                       users.role AS role,
                       tokens.token_validation AS token_validation,
                       tokens.created_at AS created_at,
                       tokens.last_used_at AS last_used_at
                FROM dbo.Users AS users
                INNER JOIN dbo.tokens AS tokens
                ON users.id = tokens.user_id
                WHERE token_validation = :validation_information
                """.trimIndent(),
            ).bind("validation_information", tokenValidationInfo.validationInfo)
            .mapTo<UserAndTokenModel>()
            .singleOrNull()
            ?.userAndToken

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        val deletions =
            handle
                .createUpdate(
                    """
                    DELETE FROM dbo.tokens 
                    WHERE user_id = :user_id 
                        AND token_validation IN (
                            SELECT token_validation FROM dbo.tokens WHERE user_id = :user_id 
                                ORDER BY last_used_at DESC OFFSET :offset
                        )
                    """.trimIndent(),
                ).bind("user_id", token.userId)
                .bind("offset", maxTokens - 1)
                .execute()

        handle
            .createUpdate(
                """
                INSERT INTO dbo.tokens(user_id, token_validation, created_at, last_used_at) 
                VALUES (:user_id, :token_validation, :created_at, :last_used_at)
                """.trimIndent(),
            ).bind("user_id", token.userId)
            .bind("token_validation", token.tokenValidationInfo.validationInfo)
            .bind("created_at", token.createdAt.epochSecond)
            .bind("last_used_at", token.lastUsedAt.epochSecond)
            .execute()
    }

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        handle
            .createUpdate(
                """
                UPDATE dbo.tokens
                SET last_used_at = :last_used_at
                WHERE token_validation = :validation_information
                """.trimIndent(),
            ).bind("last_used_at", now.epochSecond)
            .bind("validation_information", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int =
        handle
            .createUpdate(
                """
                DELETE FROM dbo.tokens
                WHERE token_validation = :validation_information
            """,
            ).bind("validation_information", tokenValidationInfo.validationInfo)
            .execute()

    override fun findById(id: Int): User? =
        handle
            .createQuery("SELECT * FROM dbo.users WHERE id = :id")
            .bind("id", id)
            .map {
                    rs, _,
                ->
                mapRow(rs)
            }
            .findOne()
            .orElse(null)

    override fun findAll(): List<User> =
        handle
            .createQuery("SELECT * FROM dbo.users")
            .map {
                    rs, _,
                ->
                mapRow(rs)
            }
            .list()

    override fun save(entity: User) {
        handle
            .createUpdate(
                """
                UPDATE dbo.users 
                SET name = :name,
                    email = :email,
                    password_validation = :passwordValidation,
                    role = :role
                WHERE id = :id
                """.trimIndent(),
            )
            .bind("id", entity.id)
            .bind("name", entity.name)
            .bind("email", entity.email)
            .bind("passwordValidation", entity.passwordValidation.validationInfo)
            .bind("role", entity.role.name)
            .execute()
    }

    override fun deleteById(id: Int) {
        handle
            .createUpdate("DELETE FROM dbo.users WHERE id = :id")
            .bind("id", id)
            .execute()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.tokens").execute()
        handle.createUpdate("DELETE FROM dbo.users").execute()
    }

    private data class UserAndTokenModel(
        val id: Int,
        val name: String,
        val email: String,
        val passwordValidation: String,
        val role: UserRole,
        val tokenValidation: String,
        val createdAt: Long,
        val lastUsedAt: Long,
    ) {
        val userAndToken: Pair<User, Token>
            get() =
                Pair(
                    User(
                        id,
                        name,
                        email,
                        PasswordValidationInfo(passwordValidation),
                        role,
                    ),
                    Token(
                        TokenValidationInfo(tokenValidation),
                        id,
                        Instant.ofEpochSecond(createdAt),
                        Instant.ofEpochSecond(lastUsedAt),
                    ),
                )
    }

    private fun mapRow(rs: ResultSet): User {
        return User(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            passwordValidation =
                PasswordValidationInfo(
                    rs.getString("password_validation"),
                ),
            role = UserRole.valueOf(rs.getString("role")),
        )
    }
}
