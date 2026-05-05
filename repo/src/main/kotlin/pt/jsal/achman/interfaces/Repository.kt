package pt.jsal.achman.interfaces

/**
 * Interface genérica de repositório para operações básicas de CRUD.
 *
 * @param T Tipo da entidade gerida pelo repositório.
 */
interface Repository<T> {
    /**
     * Obtém uma entidade pelo seu identificador.
     *
     * @param id Identificador da entidade.
     *
     * @return Entidade do tipo [T], ou null caso não exista.
     */
    fun findById(id: Int): T?

    /**
     * Obtém todas as entidades.
     *
     * @return Lista de entidades do tipo [T], ou lista vazia caso não existam.
     */
    fun findAll(): List<T>

    /**
     * Persiste uma nova entidade ou atualiza uma existente.
     *
     * @param entity Entidade a persistir ou atualizar.
     */
    fun save(entity: T)

    /**
     * Remove uma entidade pelo seu identificador.
     *
     * @param id Identificador da entidade a remover.
     */
    fun deleteById(id: Int)

    /**
     * Remove todas as entidades do repositório.
     */
    fun clear()
}
