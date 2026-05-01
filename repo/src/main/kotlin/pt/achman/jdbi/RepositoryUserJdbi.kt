package pt.achman.jdbi

import org.jdbi.v3.core.Handle
import pt.achman.interfaces.RepositoryUser
import pt.achman.token.Token
import pt.achman.token.TokenValidationInfo
import pt.achman.user.PasswordValidationInfo
import pt.achman.user.User
import java.time.Instant

class RepositoryUserJdbi(
    handle: Handle
): RepositoryUser {
    override fun createUser(
        name: String,
        email: String,
        passwordValidation: PasswordValidationInfo
    ): User {
        TODO("Not yet implemented")
    }

    override fun findByEmail(email: String): User? {
        TODO("Not yet implemented")
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? {
        TODO("Not yet implemented")
    }

    override fun createToken(token: Token, maxTokens: Int) {
        TODO("Not yet implemented")
    }

    override fun updateTokenLastUsed(token: Token, now: Instant) {
        TODO("Not yet implemented")
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        TODO("Not yet implemented")
    }

    override fun findById(id: Int): User? {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<User> {
        TODO("Not yet implemented")
    }

    override fun save(entity: User) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Int) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}