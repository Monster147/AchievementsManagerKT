package pt.jsal.achman.jdbi

import com.fasterxml.jackson.databind.ObjectMapper
import org.jdbi.v3.core.Jdbi
import pt.jsal.achman.interfaces.Transaction
import pt.jsal.achman.interfaces.TransactionManager

class TransactionManagerJdbi(
    private val jdbi: Jdbi,
    private val objectMapper: ObjectMapper,
) : TransactionManager {
    override fun <R> run(block: Transaction.() -> R): R {
        println(jdbi)
        return jdbi.inTransaction<R, Exception> { handle ->
            val transaction = TransactionInJdbi(handle, objectMapper)
            block(transaction)
        }
    }
}
