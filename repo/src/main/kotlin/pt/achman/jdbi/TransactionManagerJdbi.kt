package pt.achman.jdbi

import org.jdbi.v3.core.Jdbi
import pt.achman.interfaces.Transaction
import pt.achman.interfaces.TransactionManager

class TransactionManagerJdbi(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: Transaction.() -> R): R {
        println(jdbi)
        return jdbi.inTransaction<R, Exception> { handle ->
            val transaction = TransactionInJdbi(handle)
            block(transaction)
        }
    }
}
