package pt.achman

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.achman.interfaces.TransactionManager
import pt.achman.token.Token
import pt.achman.token.TokenEncoder
import pt.achman.token.TokenExternalInfo
import pt.achman.user.PasswordValidationInfo
import pt.achman.user.User
import pt.achman.user.UserRole
import pt.achman.user.UsersDomainConfig
import pt.achman.utils.Either
import pt.achman.utils.failure
import pt.achman.utils.success
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64.getUrlDecoder
import java.util.Base64.getUrlEncoder
import kotlin.run
import kotlin.text.compareTo

sealed class UserError {
    data object AlreadyUsedEmailAddress : UserError()

    data object InsecurePassword : UserError()

    data object UserNotFound : UserError()

    data object UserNotAdmin : UserError()
}

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
}

const val SPECIAL_CHARACTERS = "!@#\$%^&*()-_=+[]{}|\\:;\"'<>,.?/"

/**
 * Serviço responsável pela gestão do ciclo de vida dos utilizadores e autenticação.
 *
 * Responsabilidades principais:
 * - criação e consulta de utilizadores;
 * - validação e gestão de palavras-passe;
 * - gestão de papéis (roles) dos utilizadores;
 * - geração, validação e revogação de tokens de autenticação;
 * - cálculo de métricas associadas a relatórios.
 *
 * @param passwordEncoder codificador de palavras-passe.
 * @param tokenEncoder codificador de tokens.
 * @param config configuração do domínio de utilizadores.
 * @param trxManager gestor de transações usado para aceder aos repositórios dentro de unidades de trabalho.
 * @param clock fonte de tempo usada para validação temporal de tokens.
 */
@Component
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
    private val trxManager: TransactionManager,
    private val clock: Clock,
) {
    private fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    private fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    fun isSafePassword(password: String) =
        password.length >= 8 &&
            password.any { it.isDigit() } &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { SPECIAL_CHARACTERS.contains(it) }

    fun createUser(
        name: String,
        email: String,
        password: String,
    ): Either<UserError, User> {
        if (!isSafePassword(password)) {
            return failure(UserError.InsecurePassword)
        }

        val passwordValidationInfo = createPasswordValidationInformation(password)

        return trxManager.run {
            if (repoUsers.findByEmail(email) != null) {
                return@run failure(UserError.AlreadyUsedEmailAddress)
            }

            val player = repoUsers.createUser(name, email, passwordValidationInfo, UserRole.NORMAL)
            success(player)
        }
    }

    fun createToken(
        email: String,
        password: String,
    ): Either<TokenCreationError, TokenExternalInfo> {
        if ((email.isBlank()) || (password.isBlank())) {
            return failure(TokenCreationError.UserOrPasswordAreInvalid)
        }

        return trxManager.run {
            val user =
                repoUsers.findByEmail(email)
                    ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)

            if (!validatePassword(password, user.passwordValidation)) {
                return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            }

            val tokenValue = generateTokenValue()
            val now = clock.instant()
            val newToken =
                Token(
                    tokenEncoder.createValidationInformation(tokenValue),
                    user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            repoUsers.createToken(newToken, config.maxTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    getTokenExpiration(newToken),
                ),
            )
        }
    }

    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
        return trxManager.run {
            repoUsers.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }

    fun getUserByToken(token: String): User? {
        if (!canBeToken(token)) {
            return null
        }
        return trxManager.run {
            val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
            val userAndToken: Pair<User, Token>? = repoUsers.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && isTokenTimeValid(clock, userAndToken.second)) {
                repoUsers.updateTokenLastUsed(userAndToken.second, clock.instant())
                userAndToken.first
            } else {
                null
            }
        }
    }

    private fun canBeToken(token: String): Boolean =
        try {
            getUrlDecoder().decode(token).size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    private fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.instant()
        return token.createdAt <= now &&
            Duration.between(now, token.createdAt) <= config.tokenTtl &&
            Duration.between(now, token.lastUsedAt) <= config.tokenRollingTtl
    }

    private fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            getUrlEncoder().encodeToString(byteArray)
        }

    private fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }
}
