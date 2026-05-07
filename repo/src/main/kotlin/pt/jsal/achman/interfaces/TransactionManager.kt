package pt.jsal.achman.interfaces

interface TransactionManager {
    /**
     * Este método cria uma instância de Transaction, podendo
     * inicializar uma ligação JDBC, um Handle do JDBI, ou outro recurso,
     * que é depois passado como argumento ao construtor de Transaction.
     */
    fun <R> run(block: Transaction.() -> R): R
}
