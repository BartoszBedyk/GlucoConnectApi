package infrastructure

import decryptField
import encryptField
import form.*
import hashEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import javax.crypto.SecretKey
import javax.sql.DataSource

class UserDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS glucoconnectapi.users (
    id CHAR(36) PRIMARY KEY,
    first_name_encrypted TEXT,
    first_name_iv TEXT,
    last_name_encrypted TEXT,
    last_name_iv TEXT,
    email_encrypted TEXT NOT NULL,
    email_iv TEXT NOT NULL,
    email_hash VARCHAR(50) NULL,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(50), 
    is_blocked BOOLEAN NOT NULL,
    pref_unit VARCHAR(50),
    diabetes_type_encrypted TEXT,
    diabetes_type_iv TEXT
    CHECK (type IN ('ADMIN', 'PATIENT', 'DOCTOR', 'OBSERVER'))
    CHECK (pref_unit IN ('MG_PER_DL', 'MMOL_PER_L')))
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

    suspend fun createUserStepOne(createUserForm: CreateUserStepOneForm, secretKey: SecretKey): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val createUserQuery = """INSERT INTO users (id, email_encrypted, email_iv, email_hash, password, is_blocked)
VALUES (?, ?, ?, ?, ?,?) """
        val emailHash = hashEmail(createUserForm.email)
        val (emailEncrypted, emailIv) = encryptField(createUserForm.email, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(createUserQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, emailEncrypted)
                    setString(3, emailIv)
                    setString(4, emailHash)
                    setString(5, createUserForm.password)
                    setBoolean(6, false)
                }
                statement.executeUpdate()
                ///fdo

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

    suspend fun createUserStepTwo(form: CreateUserStepTwoForm, secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val updateUserNullFormQuery =
            "UPDATE users SET first_name_encrypted = ?, first_name_iv = ?, last_name_encrypted = ?, last_name_iv = ?, pref_unit = ?, diabetes_type_encrypted = ?, diabetes_type_iv = ?, user_type = ? WHERE id = ?;"

        val (diabetesEncrypted, diabetesIv) = encryptField(form.diabetes, secretKey)
        val (firstNameEncrypted, firstNameIv) = encryptField(form.firstName, secretKey)
        val (lastNameEncrypted, lastNameIv) = encryptField(form.lastName, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(updateUserNullFormQuery).use { statement ->
                statement.apply {
                    setString(1, firstNameEncrypted)
                    setString(2, firstNameIv)
                    setString(3, lastNameEncrypted)
                    setString(4, lastNameIv)
                    setString(5, form.prefUnit)
                    setString(6, diabetesEncrypted)
                    setString(7, diabetesIv)
                    setString(8, form.userType)
                    setString(9, form.id.toString())
                }
                statement.executeUpdate()
            }
        }
    }

    suspend fun createUserWithType(form: CreateUserFormWithType, secretKey: SecretKey): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val createUserQuery = """INSERT INTO users (id, email_encrypted, email_iv,email_hash, password, type, is_blocked )
VALUES (?, ?, ?, ?, ?, ?,?) """

        val emailHash = hashEmail(form.email)
        val (emailEncrypted, emailIv) = encryptField(form.email, secretKey)
        dataSource.connection.use { connection ->
            connection.prepareStatement(createUserQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, emailEncrypted)
                    setString(3, emailIv)
                    setString(4, emailHash)
                    setString(5, form.password)
                    setString(6, form.userType.toString())
                    setBoolean(7, false)
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

        dataSource.connection.use { connection ->
            try {
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

        dataSource.connection.use { connection ->
            try {
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

    suspend fun readUser(id: String, secretKey: SecretKey): User = withContext(Dispatchers.IO) {
        val readUserQuery =
            """SELECT id, first_name_encrypted, first_name_iv, last_name_encrypted, last_name_iv, email_encrypted, email_iv, type, is_blocked, pref_unit, diabetes_type_encrypted, diabetes_type_iv  FROM users WHERE id = ?"""

        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {

                        val diabetesType = decryptField(
                            resultSet.getString("diabetes_type_encrypted"),
                            resultSet.getString("diabetes_type_iv"),
                            secretKey
                        )
                        val email = decryptField(
                            resultSet.getString("email_encrypted"),
                            resultSet.getString("email_iv"),
                            secretKey
                        )
                        val firstName = decryptField(
                            resultSet.getString("first_name_encrypted"),
                            resultSet.getString("first_name_iv"),
                            secretKey
                        )

                        val lastName = decryptField(
                            resultSet.getString("last_name_encrypted"),
                            resultSet.getString("last_name_iv"),
                            secretKey
                        )

                        return@withContext User(
                            UUID.fromString(resultSet.getString("id")),
                            firstName,
                            lastName,
                            email,
                            "***password***",
                            resultSet.getString("type")?.let { UserType.valueOf(it) },
                            resultSet.getBoolean("is_blocked"),
                            resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            diabetesType.let { DiabetesType.valueOf(it) }
                        )
                    } else {
                        throw NoSuchElementException("Record with ID $id not found")
                    }
                }
            }
        }
    }

    suspend fun getAll(secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        val selectAllQuery = "SELECT * FROM users"
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {

                        val diabetesType = decryptField(
                            resultSet.getString("diabetes_type_encrypted"),
                            resultSet.getString("diabetes_type_iv"),
                            secretKey
                        )
                        val email = decryptField(
                            resultSet.getString("email_encrypted"),
                            resultSet.getString("email_iv"),
                            secretKey
                        )
                        val firstName = decryptField(
                            resultSet.getString("first_name_encrypted"),
                            resultSet.getString("first_name_iv"),
                            secretKey
                        )

                        val lastName = decryptField(
                            resultSet.getString("last_name_encrypted"),
                            resultSet.getString("last_name_iv"),
                            secretKey
                        )

                        users.add(
                            User(
                                UUID.fromString(resultSet.getString("id")),
                                firstName,
                                lastName,
                                email,
                                "#*#*#*#",
                                resultSet.getString("type")?.let { UserType.valueOf(it) },
                                resultSet.getBoolean("is_blocked"),
                                resultSet.getString("pref_unit"),
                                diabetesType.let { DiabetesType.valueOf(it) }
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

    suspend fun updateUserNulls(form: UpdateUserNullForm, secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val updateUserNullFormQuery =
            "UPDATE users SET first_name_encrypted = ?, first_name_iv = ?, last_name_encrypted = ?, last_name_iv = ?, pref_unit = ?, diabetes_type_encrypted = ?, diabetes_type_iv = ? WHERE id = ?;"

        val (diabetesEncrypted, diabetesIv) = encryptField(form.diabetes, secretKey)
        val (firstNameEncrypted, firstNameIv) = encryptField(form.firstName, secretKey)
        val (lastNameEncrypted, lastNameIv) = encryptField(form.lastName, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(updateUserNullFormQuery).use { statement ->
                statement.apply {
                    setString(1, firstNameEncrypted)
                    setString(2, firstNameIv)
                    setString(3, lastNameEncrypted)
                    setString(4, lastNameIv)
                    setString(5, form.prefUnit)
                    setString(6, diabetesEncrypted)
                    setString(7, diabetesIv)
                    setString(8, form.id.toString())
                }
                statement.executeUpdate()
            }
        }
    }

    //update user type
    suspend fun changeUserType(id: String, type: String) = withContext(Dispatchers.IO) {
        val updateUserNullFormQuery = "UPDATE users SET type = ? WHERE id = ?;"
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

    suspend fun changeUserDiabetesType(id: String, type: String, secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val updateUserNullFormQuery = "UPDATE users SET diabetes_type_encrypted = ?, diabetes_type_iv = ? = ? WHERE id = ?;"

        val (diabetesEncrypted, diabetesIv) = encryptField(type, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(updateUserNullFormQuery).use { statement ->
                statement.apply {
                    setString(1, diabetesEncrypted)
                    setString(2, diabetesIv)
                    setString(3, id)
                }
                statement.executeUpdate()
            }
        }
    }

    suspend fun getUserUnitById(id: String): PrefUnitType = withContext(Dispatchers.IO) {
        val sqlGetUnit = "SELECT pref_unit FROM users WHERE id = ? "
        dataSource.connection.use { connection ->
            connection.prepareStatement(sqlGetUnit).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext resultSet.getString("pref_unit").let { PrefUnitType.valueOf(it) }
                    } else {
                        throw NoSuchElementException("Record with ID $id not found")
                    }
                }

            }
        }
    }

    suspend fun authenticate(form: UserCredentials, secretKey: SecretKey): User = withContext(Dispatchers.IO) {
        val sqlAuthenticate = "SELECT * FROM users WHERE email_hash = ? AND is_blocked = FALSE;"
        val emailHash = hashEmail(form.email)
        dataSource.connection.use { connection ->
            connection.prepareStatement(sqlAuthenticate).use { statement ->
                statement.apply {
                    setString(1, emailHash)
                }
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {

                        val diabetesType = decryptField(
                            resultSet.getString("diabetes_type_encrypted"),
                            resultSet.getString("diabetes_type_iv"),
                            secretKey
                        )
                        val email = decryptField(
                            resultSet.getString("email_encrypted"),
                            resultSet.getString("email_iv"),
                            secretKey
                        )
                        val firstName = decryptField(
                            resultSet.getString("first_name_encrypted"),
                            resultSet.getString("first_name_iv"),
                            secretKey
                        )

                        val lastName = decryptField(
                            resultSet.getString("last_name_encrypted"),
                            resultSet.getString("last_name_iv"),
                            secretKey
                        )

                        return@withContext User(
                            UUID.fromString(resultSet.getString("id")),
                            firstName,
                            lastName,
                            email,
                            "**password**",
                            resultSet.getString("type")?.let { UserType.valueOf(it) },
                            resultSet.getBoolean("is_blocked"),
                            resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            diabetesType.let{DiabetesType.valueOf(it)}
                        )
                    } else {
                        throw NoSuchElementException("These credentials are incorrect.")
                    }

                }
            }
        }


    }

    suspend fun authenticateHash(form: UserCredentials): String = withContext(Dispatchers.IO) {
        val sqlAuthenticate = "SELECT password FROM users WHERE email_hash = ? AND is_blocked = FALSE;"

        val emailHash = hashEmail(form.email)
        dataSource.connection.use { connection ->
            connection.prepareStatement(sqlAuthenticate).use { statement ->
                statement.apply {
                    setString(1, emailHash)
                }
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val passwordHash: String = resultSet.getString("password")
                        return@use passwordHash;
                    } else {
                        throw NoSuchElementException("These credentials are incorrect.")
                    }

                }
            }
        }
    }


    suspend fun observe(partOne: String, partTwo: String, secretKey: SecretKey): User = withContext(Dispatchers.IO) {
        val readUserQuery =
            """SELECT id, first_name_encrypted,first_name_iv, last_name_encrypted, last_name_iv, email_encrypted, email_iv, type, is_blocked, pref_unit, diabetes_type_encrypted, diabetes_type_iv  FROM users WHERE id LIKE ?"""

        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserQuery).use { statement ->
                val pattern = "$partOne%$partTwo"
                statement.setString(1, pattern)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {

                        val diabetesType = decryptField(
                            resultSet.getString("diabetes_type_encrypted"),
                            resultSet.getString("diabetes_type_iv"),
                            secretKey
                        )
                        val firstName = decryptField(
                            resultSet.getString("first_name_encrypted"),
                            resultSet.getString("first_name_iv"),
                            secretKey
                        )

                        val lastName = decryptField(
                            resultSet.getString("last_name_encrypted"),
                            resultSet.getString("last_name_iv"),
                            secretKey
                        )

                        val email = decryptField(
                            resultSet.getString("email_encrypted"),
                            resultSet.getString("email_iv"),
                            secretKey
                        )

                        return@withContext User(
                            UUID.fromString(resultSet.getString("id")),
                            firstName,
                            lastName,
                            email,
                            "***password***",
                            resultSet.getString("type")?.let { UserType.valueOf(it) },
                            resultSet.getBoolean("is_blocked"),
                            resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it) }?.toString(),
                            diabetesType.let{DiabetesType.valueOf(it)}
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