package pt.jsal.achman

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.jsal.achman.interfaces.TransactionManager
import pt.jsal.achman.token.Token
import pt.jsal.achman.token.TokenEncoder
import pt.jsal.achman.token.TokenExternalInfo
import pt.jsal.achman.user.PasswordValidationInfo
import pt.jsal.achman.user.User
import pt.jsal.achman.user.UserRole
import pt.jsal.achman.user.UsersDomainConfig
import pt.jsal.achman.utils.Either
import pt.jsal.achman.utils.failure
import pt.jsal.achman.utils.success
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64.getUrlDecoder
import java.util.Base64.getUrlEncoder
import kotlin.run

/**
 * Representa os possíveis erros associados às operações sobre utilizadores.
 */
sealed class UserError {
    /**
     * Indica que o email fornecido já está a ser utilizado por outro utilizador.
     */
    data object AlreadyUsedEmailAddress : UserError()

    /**
     * Indica que a palavra-passe não cumpre os requisitos mínimos de segurança.
     */
    data object InsecurePassword : UserError()

    /**
     * Indica que o utilizador não foi encontrado.
     */
    data object UserNotFound : UserError()

    /**
     * Indica que o utilizador não tem permissões de administrador.
     */
    data object UserNotAdmin : UserError()
}

/**
 * Representa os possíveis erros na criação de tokens de autenticação.
 */
sealed class TokenCreationError {
    /**
     * Indica que as credenciais fornecidas (email ou palavra-passe) são inválidas.
     */
    data object UserOrPasswordAreInvalid : TokenCreationError()
}

/**
 * Conjunto de caracteres especiais aceites na validação de palavras-passe.
 *
 * Utilizado para garantir que as palavras-passe contêm diversidade suficiente
 * de caracteres, aumentando a sua robustez contra ataques.
 */
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
    /**
     * Valida uma palavra-passe contra a informação de validação armazenada.
     *
     * @param password Palavra-passe em texto simples.
     * @param validationInfo Informação de validação da palavra-passe.
     *
     * @return `true` se a palavra-passe for válida, `false` caso contrário.
     */
    private fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    /**
     * Cria informação de validação para uma palavra-passe.
     *
     * @param password Palavra-passe em texto simples.
     *
     * @return [PasswordValidationInfo] contendo a representação codificada da palavra-passe.
     */
    private fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    /**
     * Verifica se uma palavra-passe cumpre os requisitos mínimos de segurança.
     *
     * @param password Palavra-passe a validar.
     *
     * @return `true` se a palavra-passe for considerada segura, `false` caso contrário.
     */
    fun isSafePassword(password: String) =
        password.length >= 8 &&
            password.any { it.isDigit() } &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { SPECIAL_CHARACTERS.contains(it) }

    /**
     * Cria um utilizador.
     *
     * Valida a segurança da palavra-passe, se o email é único
     * e a existência dos papéis associados.
     *
     * @param name Nome do utilizador.
     * @param email Endereço de email.
     * @param password Palavra-passe em texto claro.
     * @param roles Lista de identificadores de papéis.
     *
     * @return [User] criado, ou um erro do tipo [UserError].
     */
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

    /**
     * Cria um token de autenticação para um utilizador.
     *
     * Valida as credenciais e gera um novo token com informação temporal associada.
     *
     * @param email Endereço de email.
     * @param password Palavra-passe.
     *
     * @return Informação externa do token, ou erro do tipo [TokenCreationError].
     */
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

    /**
     * Revoga um token de autenticação.
     *
     * @param token Valor do token.
     *
     * @return `true` após a revogação.
     */
    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
        return trxManager.run {
            repoUsers.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }

    /**
     * Obtém um utilizador com base num token válido.
     *
     * Valida o formato e validade temporal do token.
     *
     * @param token Valor do token.
     *
     * @return [User] correspondente, ou `null` se o token for inválido.
     */
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

    /**
     * Verifica se uma string pode representar um token válido.
     *
     * A validação consiste em tentar descodificar o token em Base64 URL-safe
     * e verificar se o tamanho corresponde ao esperado.
     *
     * @param token Valor do token.
     *
     * @return `true` se o token tiver um formato válido, `false` caso contrário.
     */
    private fun canBeToken(token: String): Boolean =
        try {
            getUrlDecoder().decode(token).size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    /**
     * Verifica se um token é temporalmente válido.
     *
     * Um token é considerado válido se:
     * - ainda não ultrapassou o tempo máximo absoluto (`tokenTtl`);
     * - ainda não ultrapassou o tempo máximo de inatividade (`tokenRollingTtl`);
     * - a data de criação não está no futuro.
     *
     * @param clock fonte de tempo atual.
     * @param token token a validar.
     *
     * @return `true` se o token for válido, `false` caso contrário.
     */
    private fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.instant()
        return token.createdAt <= now &&
            Duration.between(now, token.createdAt) <= config.tokenTtl &&
            Duration.between(now, token.lastUsedAt) <= config.tokenRollingTtl
    }

    /**
     * Gera um novo valor de token aleatório.
     *
     * O token é gerado com base em bytes aleatórios seguros e codificado
     * em Base64 URL-safe.
     *
     * @return valor do token em formato string.
     */
    private fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            getUrlEncoder().encodeToString(byteArray)
        }

    /**
     * Calcula o instante de expiração de um token.
     *
     * A expiração corresponde ao mínimo entre:
     * - o tempo absoluto desde a criação (`tokenTtl`);
     * - o tempo relativo desde a última utilização (`tokenRollingTtl`).
     *
     * @param token token para o qual calcular a expiração.
     *
     * @return instante em que o token expira.
     */
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
