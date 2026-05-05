package pt.jsal.achman.interfaces

import pt.jsal.achman.token.Token
import pt.jsal.achman.token.TokenValidationInfo
import pt.jsal.achman.user.PasswordValidationInfo
import pt.jsal.achman.user.User
import pt.jsal.achman.user.UserRole
import java.time.Instant

/**
 * Repositório de operações sobre utilizadores.
 */
interface RepositoryUser : pt.jsal.achman.interfaces.Repository<User> {
    /**
     * Cria um utilizador no sistema.
     *
     * @param name Nome do utilizador.
     * @param email Email do utilizador (deve ser único).
     * @param passwordValidation Informação de validação da palavra-passe (encriptação da palavra-passe).
     *
     * @return [User] criado.
     */
    fun createUser(
        name: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
        role: UserRole,
    ): User

    /**
     * Procura um utilizador pelo seu email.
     *
     * @param email Email a procurar.
     *
     * @return [User] correspondente, ou null caso não exista.
     */
    fun findByEmail(email: String): User?

    /**
     * Procura utilizadores pela sua role.
     *
     * @param role Role a pesquisar
     *
     * @return Lista de [User]s correspondentes à role, ou lista vazia caso não existam.
     */
    fun findByRole(role: UserRole): List<User>

    /**
     * Atualiza a role do utilizador.
     *
     * @param user Utilizador a atualizar a role
     * @param role Role para atualizar
     *
     * @return [User] com a role atualizada
     */
    fun updateRole(
        user: User,
        role: UserRole,
    ): User

    /**
     * Obtém o utilizador associado a um token válido.
     *
     * @param tokenValidationInfo Informação de validação do token.
     *
     * @return Par composto por [User] e [Token], ou null caso não exista.
     */
    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    /**
     * Cria e persiste um novo token de autenticação.
     *
     * @param token Token a criar.
     * @param maxTokens Número máximo de tokens permitidos por utilizador.
     */
    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    /**
     * Atualiza o timestamp da última utilização de um token.
     *
     * @param token Token a atualizar.
     * @param now Instante atual a registar como última utilização.
     */
    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    /**
     * Remove um token com base na sua informação de validação.
     *
     * @param tokenValidationInfo Informação de validação do token.
     *
     * @return Número de tokens removidos.
     */
    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}
