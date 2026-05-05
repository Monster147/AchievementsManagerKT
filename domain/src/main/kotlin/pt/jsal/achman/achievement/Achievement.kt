package pt.jsal.achman.achievement

/**
 * Representa uma conquista associada a um jogo.
 *
 * @property id Identificador interno único da conquista.
 * @property apiName Nome técnico ou identificador utilizado pela API externa.
 * @property name Nome visível da conquista.
 * @property icon URL ou caminho do ícone da conquista.
 * @property description Descrição da conquista.
 * @property gameId Identificador interno do jogo ao qual a conquista pertence.
 */
data class Achievement(
    val id: Int,
    val apiName: String,
    val name: String,
    val icon: String,
    val description: String,
    val gameId: Int,
)
