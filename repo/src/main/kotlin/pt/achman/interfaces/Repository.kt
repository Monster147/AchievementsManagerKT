package pt.achman.interfaces

/**
 * Interface genérica de repositório para operações básicas de CRUD.
 */
interface Repository<T> {
    fun findById(id: Int): T? // Obtém uma entidade pelo seu identificador

    fun findAll(): List<T> // Obtém todas as entidades

    fun save(entity: T) // Persiste uma nova entidade ou actualiza uma existente

    fun deleteById(id: Int) // Remove uma entidade pelo seu identificador

    fun clear() // Remove todas as entidades
}
