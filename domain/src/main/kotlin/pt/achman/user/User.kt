package pt.achman.user

/**
 * Representa um utilizador do sistema.
 *
 * Um utilizador possui informação identificativa, credenciais de autenticação
 * e um conjunto de roles que determinam as suas permissões no sistema.
 *
 * @property id Identificador único do utilizador.
 * @property name Nome do utilizador.
 * @property email Endereço de email do utilizador (tipicamente único no sistema).
 * @property passwordValidation Informação associada à validação da password.
 *
 * @constructor Cria uma instância de [User] com os dados fornecidos.
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val passwordValidation: PasswordValidationInfo,
)
