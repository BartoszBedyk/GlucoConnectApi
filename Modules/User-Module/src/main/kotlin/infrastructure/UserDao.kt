package infrastructure

import form.*
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
            CREATE TABLE IF NOT EXISTS glucoconnectapi.users (
    id CHAR(36) PRIMARY KEY,
    first_name VARCHAR(40),
    last_name VARCHAR(40),
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(50), 
    is_blocked BOOLEAN NOT NULL,
    pref_unit VARCHAR(50),
    diabetes_type VARCHAR(20) 
    CHECK (type IN ('ADMIN', 'PATIENT', 'DOCTOR', 'OBSERVER'))
    CHECK (pref_unit IN ('MG_PER_DL', 'MMOL_PER_L'))
    CHECK (diabetes_type IN ('TYPE_1', 'TYPE_2', 'GESTATIONAL', 'LADA', 'MODY', 'NONE')));
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

    suspend fun createUserWithType(form: CreateUserFormWithType): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val createUserQuery = """INSERT INTO users (id, email, password, type, is_blocked )
VALUES (?, ?, ?, ?, ?) """
        dataSource.connection.use { connection ->
            connection.prepareStatement(createUserQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, form.email)
                    setString(3, form.password)
                    setString(4, form.userType.toString())
                    setBoolean(5, false)
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

    suspend fun blockUser(id: String) = withContext(Dispatchers.IO) {
        val blockUserQuery = """UPDATE users
SET is_blocked = ?
WHERE id = ?;"""

        dataSource.connection.use {
            connection ->
            try{
                connection.prepareStatement(blockUserQuery).use { statement ->
                    statement.apply {
                        setBoolean(1, true)
                        setString(2, id)

                    }
                    statement.executeUpdate()
                }
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            }
        }

    }

    suspend fun unblockUser(id: String) = withContext(Dispatchers.IO) {
        val blockUserQuery = """UPDATE users
SET is_blocked = ?
WHERE id = ?;"""

        dataSource.connection.use {
                connection ->
            try{
                connection.prepareStatement(blockUserQuery).use { statement ->
                    statement.apply {
                        setBoolean(1, false)
                        setString(2, id.toString())

                    }
                    statement.executeUpdate()
                }
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            }
        }

    }

    suspend fun readUser(id: String): User = withContext(Dispatchers.IO) {
        val readUserQuery ="""SELECT id, first_name, last_name, email, password, type, is_blocked, pref_unit, diabetes_type FROM users WHERE id = ?"""

        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                             return@withContext User(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getString("first_name"),
                                resultSet.getString("last_name"),
                                resultSet.getString("email"),
                                resultSet.getString("password"),
                                resultSet.getString("type")?.let { UserType.valueOf(it) },
                                resultSet.getBoolean("is_blocked"),
                                resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it)}.toString(),
                                 resultSet.getString("diabetes_type")?.let { DiabetesType.valueOf(it)}.toString()
                            )
                } else {
                throw NoSuchElementException("Record with ID $id not found")
            }
            }
        }
        }
    }

    suspend fun getAll() = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        val selectAllQuery ="SELECT * FROM users"
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        users.add(
                            User(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getString("first_name"),
                                resultSet.getString("last_name"),
                                resultSet.getString("email"),
                                "#*#*#*#",
                                resultSet.getString("type")?.let { UserType.valueOf(it) },
                                resultSet.getBoolean("is_blocked"),
                                resultSet.getString("pref_unit"),
                                resultSet.getString("diabetes_type")?.let { DiabetesType.valueOf(it) }.toString()
                            )
                        )
                    }
                }
            }
            return@withContext users
        }

    }

    suspend fun updateUnit(form: UpdatePrefUnit) = withContext(Dispatchers.IO) {
        val updatePrefUnit = "UPDATE users SET pref_unit = ? WHERE id = ?;"
        dataSource.connection.use { connection ->
            connection.prepareStatement(updatePrefUnit).use { statement ->
                statement.apply {
                    setString(1, form.newUnit.toString())
                    setString(2, form.id.toString())
                }
                statement.executeUpdate()
            }
        }
    }

    suspend fun updateUserNulls(form: UpdateUserNullForm) = withContext(Dispatchers.IO) {
        val updateUserNullFormQuery ="UPDATE users SET first_name = ?, last_name = ?, pref_unit = ?, diabetes_type = ? WHERE id = ?;"
        dataSource.connection.use { connection ->
            connection.prepareStatement(updateUserNullFormQuery).use { statement ->
                statement.apply {
                    setString(1, form.firstName)
                    setString(2, form.lastName)
                    setString(3, form.prefUnit)
                    setString(4, form.diabetes)
                    setString(5, form.id.toString())
                }
                statement.executeUpdate()
            }
        }
    }

    //update user type
    suspend fun changeUserType(id: String, type: String) = withContext(Dispatchers.IO){
        val updateUserNullFormQuery ="UPDATE users SET type = ? WHERE id = ?;"
        dataSource.connection.use { connection ->
            connection.prepareStatement(updateUserNullFormQuery).use { statement ->
                statement.apply {
                    setString(1, type)
                    setString(2, id)
                }
                statement.executeUpdate()
            }
        }
    }

    suspend fun changeUserDiabetesType(id: String, type: String) = withContext(Dispatchers.IO){
        val updateUserNullFormQuery ="UPDATE users SET diabetes_type = ? WHERE id = ?;"
        dataSource.connection.use { connection ->
            connection.prepareStatement(updateUserNullFormQuery).use { statement ->
                statement.apply {
                    setString(1, type)
                    setString(2, id)
                }
                statement.executeUpdate()
            }
        }
    }

    suspend fun getUserUnitById(id : String): PrefUnitType = withContext(Dispatchers.IO){
        val sqlGetUnit = "SELECT pref_unit FROM users WHERE id = ? "
        dataSource.connection.use { connection ->
            connection.prepareStatement(sqlGetUnit).use {statement ->
                statement.setString(1, id)
                statement.executeQuery().use {
                    resultSet ->
                        if(resultSet.next()){
                            return@withContext resultSet.getString("pref_unit").let { PrefUnitType.valueOf(it) }
                        }else{
                            throw NoSuchElementException("Record with ID $id not found")
                        }
                }

            }
        }
    }

    suspend fun authenticate(form: UserCredentials): User = withContext(Dispatchers.IO){
        val sqlAuthenticate = "SELECT * FROM users WHERE email = ? AND password = ? AND is_blocked = FALSE;"
        dataSource.connection.use { connection ->
            connection.prepareStatement(sqlAuthenticate).use { statement ->
                statement.apply {
                    setString(1, form.email)
                    setString(2, form.password)
                }
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext User(
                            UUID.fromString(resultSet.getString("id")),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getString("email"),
                            resultSet.getString("password"),
                            resultSet.getString("type")?.let { UserType.valueOf(it) },
                            resultSet.getBoolean("is_blocked"),
                            resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it)}.toString(),
                            resultSet.getString("diabetes_type")?.let { DiabetesType.valueOf(it) }.toString()
                        )
                    } else {
                        throw NoSuchElementException("These credentials are incorrect.")
                    }

                }
            }
        }
    }


    suspend fun observe(partOne: String, partTwo: String): User = withContext(Dispatchers.IO) {
        val readUserQuery = """SELECT id, first_name, last_name, email, password, type, is_blocked, pref_unit FROM users WHERE id LIKE ?"""

        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserQuery).use { statement ->
                val pattern = "$partOne%$partTwo"
                statement.setString(1, pattern)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext User(
                            UUID.fromString(resultSet.getString("id")),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getString("email"),
                            resultSet.getString("password"),
                            resultSet.getString("type")?.let { UserType.valueOf(it) },
                            resultSet.getBoolean("is_blocked"),
                            resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it) }?.toString(),
                            resultSet.getString("diabetes_type")?.let { DiabetesType.valueOf(it) }.toString()
                        )
                    } else {
                        throw NoSuchElementException("Record with $partOne and $partTwo not found")
                    }
                }
            }
        }

    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        val deleteQuery = """DELETE FROM users WHERE id = ?"""

        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, userId)
                val affectedRows = statement.executeUpdate()

                if (affectedRows == 0) {
                    throw NoSuchElementException("User with ID $userId not found for deletion")
                }
            }
        }
    }

    suspend fun resetPassword(userId: String, newPassword: String) = withContext(Dispatchers.IO) {
        val resetQuery = """UPDATE users SET password = ? WHERE id = ?"""

        dataSource.connection.use { connection ->
            connection.prepareStatement(resetQuery).use { statement ->
                statement.setString(1, newPassword)
                statement.setString(2, userId)

                val affectedRows = statement.executeUpdate()

                if (affectedRows == 0) {
                    throw NoSuchElementException("User with ID $userId not found for password reset")
                }
            }
        }
    }




}