package infrastructure

import form.CreateUserForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import javax.sql.DataSource

class UserDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS public.users (
    id CHAR(36) PRIMARY KEY,
    first_name VARCHAR(40),
    last_name VARCHAR(40),
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(50), 
    is_blocked BOOLEAN NOT NULL,
    prefUnit VARCHAR(50)
    CHECK (type IN ('ADMIN', 'PATIENT', 'DOCTOR', 'OBSERVER')));
        """
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                try {
                    statement.executeUpdate(createTableQuery)
                } catch (e: SQLException) {
                    if (!e.message?.contains("already exists")!!) {
                        throw e
                    } else {
                        // idk ale wymaga else nwm czemu
                    }
                }
            }
        }
    }

     suspend fun createUser(createUserForm: CreateUserForm): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val createUserQuery = """INSERT INTO users (id, email, password, is_blocked)
VALUES (?, ?, ?, ?) """
        dataSource.connection.use { connection ->
            connection.prepareStatement(createUserQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, createUserForm.email)
                    setString(3, createUserForm.password)
                    setBoolean(4, false)
                }
                statement.executeUpdate()

                statement.generatedKeys.use { generatedKeys ->
                    if (generatedKeys.next()) {
                        return@withContext id
                    } else {
                        throw IllegalStateException("Generated keys not found")
                    }
                }
            }
        }
    }
}