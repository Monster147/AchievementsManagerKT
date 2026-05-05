package pt.jsal.achman.jdbi

object Environment {
    fun getDbUrl() = System.getenv(KEY_DB_URL)

    private const val KEY_DB_URL = "DB_URL"
}
