package infrastructure

import decryptField
import encryptField
import form.CreateUserFormWithType
import form.CreateUserStepOneForm
import form.CreateUserStepTwoForm
import form.DiabetesType
import form.PrefUnitType
import form.UpdatePrefUnit
import form.UpdateUserNullForm
import form.User
import form.UserCredentials
import form.UserType
import hashEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.UUID
import javax.crypto.SecretKey
import javax.sql.DataSource

class UserDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                try {
                    statement.executeUpdate(SqlQueries.CREATE_USER_TABLE)
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

    // CREATE/PUT User functionalities for register process
    // 1. Creates user entity, 2. Fills user entity with data, 3. Different methods to create user

    // 1. Create empty user "root" data, logging is available,
    // but it can cause errors and user has to use createUserStepTwo
    suspend fun createUserStepOne(createUserForm: CreateUserStepOneForm, secretKey: SecretKey): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val emailHash = hashEmail(createUserForm.email)
        val (emailEncrypted, emailIv) = encryptField(createUserForm.email, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.CREATE_USER_STEP_ONE, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, emailEncrypted)
                    setString(3, emailIv)
                    setString(4, emailHash)
                    setString(5, createUserForm.password)
                    setBoolean(6, false)
                    setTimestamp(7, Timestamp(System.currentTimeMillis()))
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

    // 2. Updates additional data of newly created user, next step after createUserStepOne
    suspend fun createUserStepTwo(form: CreateUserStepTwoForm, secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val (diabetesEncrypted, diabetesIv) = encryptField(form.diabetes, secretKey)
        val (firstNameEncrypted, firstNameIv) = encryptField(form.firstName, secretKey)
        val (lastNameEncrypted, lastNameIv) = encryptField(form.lastName, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.CREATE_USER_STEP_TWO).use { statement ->
                statement.apply {
                    setString(1, firstNameEncrypted)
                    setString(2, firstNameIv)
                    setString(3, lastNameEncrypted)
                    setString(4, lastNameIv)
                    setString(5, form.prefUnit)
                    setString(6, diabetesEncrypted)
                    setString(7, diabetesIv)
                    setString(8, form.userType)
                    setTimestamp(9, Timestamp(System.currentTimeMillis()))
                    setString(10, form.id.toString())
                }
                statement.executeUpdate()
            }
        }
    }

    // 3. Obsolete method is implemented, but you shouldn't use it soon.
    suspend fun createUserWithType(form: CreateUserFormWithType, secretKey: SecretKey): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()

        val emailHash = hashEmail(form.email)
        val (emailEncrypted, emailIv) = encryptField(form.email, secretKey)
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.CREATE_USER_WITH_TYPE, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, emailEncrypted)
                    setString(3, emailIv)
                    setString(4, emailHash)
                    setString(5, form.password)
                    setString(6, form.userType.toString())
                    setBoolean(7, false)
                    setTimestamp(8, Timestamp(System.currentTimeMillis()))
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

    // ALL GET/READ Methods for user data
    // 1. Returns user by id, 2. Returns All users, 3. Returns user unit

    // 1. Returns a User object by user_id/id, but password is just a "password" string
    suspend fun getUserById(id: String, secretKey: SecretKey): User = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_USER_BY_ID).use { statement ->
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
                            resultSet.getString("user_type")?.let { UserType.valueOf(it) },
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

    // 2. Returns all users from the database doesn't matter of their blocked, deleted status.
    suspend fun getAll(secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_ALL_USERS).use { statement ->
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
                                resultSet.getString("user_type")?.let { UserType.valueOf(it) },
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

    // 3. Return a user glucose unit type by user id
    suspend fun getUserUnitById(id: String): PrefUnitType = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_UNIT_BY_ID).use { statement ->
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

    // PUT FUNCTIONS FOR USER DATA
    // 1. Update user glucose unit, 2. Update user data nulls etc., 3.Change/set user type, 4. Change/set user diabetes type

    // 1. Updates user glucose unit available mg/dL and mmol/L if want to extend it, you need to alter table rules and update PrefUnitType enum class
    suspend fun updateUnit(form: UpdatePrefUnit) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.UPDATE_UNIT).use { statement ->
                statement.apply {
                    setString(1, form.newUnit.toString())
                    setString(2, form.id.toString())
                }
                statement.executeUpdate()
            }
        }
    }

    // 2. Updated null values or is used to update existed user data. It Is the third unforced, not required step of register.
    suspend fun updateUserProfileData(form: UpdateUserNullForm, secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val (diabetesEncrypted, diabetesIv) = encryptField(form.diabetes, secretKey)
        val (firstNameEncrypted, firstNameIv) = encryptField(form.firstName, secretKey)
        val (lastNameEncrypted, lastNameIv) = encryptField(form.lastName, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.UPDATE_USER_PROFILE_DATA).use { statement ->
                statement.apply {
                    setString(1, firstNameEncrypted)
                    setString(2, firstNameIv)
                    setString(3, lastNameEncrypted)
                    setString(4, lastNameIv)
                    setString(5, form.prefUnit)
                    setString(6, diabetesEncrypted)
                    setString(7, diabetesIv)
                    setTimestamp(8, Timestamp(System.currentTimeMillis()))
                    setString(9, form.id.toString())
                }
                statement.executeUpdate()
            }
        }
    }

    // 3. Update user Type on base of ProhibitedUserType (just Patient and Observer). If u want to create a doctor or admin, u have to use a direct sql query on the database.
    suspend fun updateUserType(id: String, type: String) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.UPDATE_USER_TYPE).use { statement ->
                statement.apply {
                    setString(1, type)
                    setTimestamp(2, Timestamp(System.currentTimeMillis()))
                    setString(3, id)
                }
                statement.executeUpdate()
            }
        }
    }

    // 4. Update a user diabetes type, available types are in DiabetesType Enum Class
    suspend fun updateUserDiabetesType(id: String, type: String, secretKey: SecretKey) = withContext(Dispatchers.IO) {
        val (diabetesEncrypted, diabetesIv) = encryptField(type, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.UPDATE_USER_DIABETES_TYPE).use { statement ->
                statement.apply {
                    setString(1, diabetesEncrypted)
                    setString(2, diabetesIv)
                    setTimestamp(3, Timestamp(System.currentTimeMillis()))
                    setString(4, id)
                }
                statement.executeUpdate()
            }
        }
    }

    // Authentication functionalities
    // 1. Main log in function, 2.Hash authentication (1'st. helper),
    suspend fun authenticate(form: UserCredentials, secretKey: SecretKey): User = withContext(Dispatchers.IO) {
        val emailHash = hashEmail(form.email)
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.AUTHENTICATE).use { statement ->
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
                            resultSet.getString("user_type")?.let { UserType.valueOf(it) },
                            resultSet.getBoolean("is_blocked"),
                            resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            diabetesType.let { DiabetesType.valueOf(it) }
                        )
                    } else {
                        throw NoSuchElementException("These credentials are incorrect.")
                    }
                }
            }
        }
    }

    // 2. Helps with authentication step one
    suspend fun authenticateHash(form: UserCredentials): String = withContext(Dispatchers.IO) {
        val emailHash = hashEmail(form.email)
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.AUTHENTICATE_HASH).use { statement ->
                statement.apply {
                    setString(1, emailHash)
                }
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val passwordHash: String = resultSet.getString("password")
                        return@use passwordHash
                    } else {
                        throw NoSuchElementException("These credentials are incorrect.")
                    }
                }
            }
        }
    }

    // If deliver correct code creates a first step to bind an account. Read more in the Observers Module
    suspend fun observe(partOne: String, partTwo: String, secretKey: SecretKey): User = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.OBSERVE_USER).use { statement ->
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
                            resultSet.getString("user_type")?.let { UserType.valueOf(it) },
                            resultSet.getBoolean("is_blocked"),
                            resultSet.getString("pref_unit")?.let { PrefUnitType.valueOf(it) }?.toString(),
                            diabetesType.let { DiabetesType.valueOf(it) }
                        )
                    } else {
                        throw NoSuchElementException("Record with $partOne and $partTwo not found")
                    }
                }
            }
        }
    }

    // Management of user state 1. Block, 2.UnBlock 3.Soft Delete for 62 days, 4. Hard delete of all data, 5. Reset password

    // 1. Set is_blocked TRUE user cannot log in anymore, TODO "automatically logout user on the mobile application"
    suspend fun blockUser(id: String) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement(SqlQueries.BLOCK_USER).use { statement ->
                    statement.apply {
                        setBoolean(1, true)
                        setTimestamp(2, Timestamp(System.currentTimeMillis()))
                        setString(3, id)
                    }
                    statement.executeUpdate()
                }
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            }
        }
    }

    // 2. Set is_blocked FALSE user can use his account as before
    suspend fun unblockUser(id: String) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement(SqlQueries.UNBLOCK_USER).use { statement ->
                    statement.apply {
                        setBoolean(1, false)
                        setTimestamp(2, Timestamp(System.currentTimeMillis()))
                        setString(3, id)
                    }
                    statement.executeUpdate()
                }
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            }
        }
    }

    // 3. Set is_deleted for TRUE, if an account is deleted for more than 61 days, all data is fully deleted
    suspend fun softDeleteUser(id: String) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement(SqlQueries.SOFT_DELETE).use { statement ->
                    statement.apply {
                        setBoolean(1, true)
                        setTimestamp(2, Timestamp(System.currentTimeMillis()))
                        setString(3, id)
                    }
                    statement.executeUpdate()
                }
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            }
        }
    }

    // 4.HARD DELETE USER DO NOT USE IT WITH ANY ENDPOINT!
    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.HARD_DELETE).use { statement ->
                statement.setString(1, userId)
                val affectedRows = statement.executeUpdate()

                if (affectedRows == 0) {
                    throw NoSuchElementException("User with ID $userId not found for deletion")
                }
            }
        }
    }

    // TODO "Add am emailing service for sending reset password code"
    // 5. Resets user password, DO NOT USE
    suspend fun resetPassword(userId: String, newPassword: String) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.RESET_PASSWORD).use { statement ->
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
