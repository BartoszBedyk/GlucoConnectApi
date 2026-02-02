import org.jetbrains.exposed.sql.transactions.transaction

fun runSql(resource: String) {
    val sql = Thread.currentThread()
        .contextClassLoader
        .getResource(resource)
        ?.readText()
        ?: error("SQL file not found: $resource")

    transaction {
        sql.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { exec(it) }
    }
}
