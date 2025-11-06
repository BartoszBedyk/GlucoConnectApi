package infrastructure

import decryptField
import encryptField
import form.CreateUserMedication
import form.GetMedicationForm
import form.UserMedication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.UUID
import javax.crypto.SecretKey
import javax.sql.DataSource

class UserMedicationDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                try {
                    statement.executeUpdate(SqlQueriesUserMedication.CREATE_USER_MEDICATION_TABLE)
                } catch (e: SQLException) {
                    if (!e.message?.contains("already exists")!!) {
                        throw e
                    } else {
                        // works correctly
                    }
                }
            }
        }
    }

    // POST/CREATE 1. Create user medication

    // 1. Create user medication on base of a user and a medication
    suspend fun createUserMedication(createUserMedicationForm: CreateUserMedication, secretKey: SecretKey): UUID =
        withContext(Dispatchers.IO) {
            val (dosageEncrypted, dosageIv) = encryptField(createUserMedicationForm.dosage, secretKey)
            val (frequencyEncrypted, frequencyIv) = encryptField(createUserMedicationForm.frequency, secretKey)
            val (notesEncrypted, notesIv) = encryptField(createUserMedicationForm.notes ?: "", secretKey)

            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    SqlQueriesUserMedication.CREATE_USER_MEDICATION,
                    Statement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    statement.apply {
                        setString(1, createUserMedicationForm.id.toString())
                        setString(2, createUserMedicationForm.userId.toString())
                        setString(3, createUserMedicationForm.medicationId.toString())
                        setString(4, dosageEncrypted)
                        setString(5, dosageIv)
                        setString(6, frequencyEncrypted)
                        setString(7, frequencyIv)
                        setTimestamp(8, createUserMedicationForm.startDate?.let { Timestamp(it.time) })
                        setTimestamp(9, createUserMedicationForm.endDate?.let { Timestamp(it.time) })
                        setString(10, notesEncrypted)
                        setString(11, notesIv)
                    }
                    statement.executeUpdate()

                    statement.use {
                        return@withContext createUserMedicationForm.id
                    }
                }
            }
        }

    // Get/read 1. Returns UM by user.id, 2. Returns um by um.id, 3. Pair of user_id and medication_id.

    // 1. Returns user medication's list for user_id, for actual date span and when it's not softly deleted.
    suspend fun getUserMedicationByUserId(userId: String, secretKey: SecretKey): List<UserMedication> = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueriesUserMedication.GET_USER_MEDICATION_BY_USER_ID).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { resultSet ->
                    val userMedications = mutableListOf<UserMedication>()

                    while (resultSet.next()) {
                        val dosage = decryptField(
                            resultSet.getString("dosage_encrypted"),
                            resultSet.getString("dosage_iv"),
                            secretKey
                        )
                        val frequency = decryptField(
                            resultSet.getString("frequency_encrypted"),
                            resultSet.getString("frequency_iv"),
                            secretKey
                        )
                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )

                        userMedications.add(
                            UserMedication(
                                id = UUID.fromString(resultSet.getString("id")),
                                userId = UUID.fromString(resultSet.getString("user_id")),
                                dosage = dosage,
                                frequency = frequency,
                                startDate = resultSet.getTimestamp("start_date"),
                                endDate = resultSet.getTimestamp("end_date"),
                                notes = notes,
                                medicationName = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                manufacturer = resultSet.getString("manufacturer"),
                                form = resultSet.getString("form"),
                                strength = resultSet.getString("strength"),
                                medicationId = UUID.fromString(resultSet.getString("medication_id"))
                            )
                        )
                    }
                    return@withContext userMedications
                }
            }
        }
    }

    // 2. Returns user_medication by user_medication_id even if it is deleted or null.
    suspend fun getUserMedicationById(umId: String, secretKey: SecretKey): UserMedication = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueriesUserMedication.GET_USER_MEDICATION_BY_ID).use { statement ->
                statement.setString(1, umId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val dosage = decryptField(
                            resultSet.getString("dosage_encrypted"),
                            resultSet.getString("dosage_iv"),
                            secretKey
                        )
                        val frequency = decryptField(
                            resultSet.getString("frequency_encrypted"),
                            resultSet.getString("frequency_iv"),
                            secretKey
                        )
                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )
                        return@withContext UserMedication(
                            id = UUID.fromString(resultSet.getString("id")),
                            userId = UUID.fromString(resultSet.getString("user_id")),
                            dosage = dosage,
                            frequency = frequency,
                            startDate = resultSet.getTimestamp("start_date"),
                            endDate = resultSet.getTimestamp("end_date"),
                            notes = notes,
                            medicationName = resultSet.getString("name"),
                            description = resultSet.getString("description"),
                            manufacturer = resultSet.getString("manufacturer"),
                            form = resultSet.getString("form"),
                            strength = resultSet.getString("strength"),
                            medicationId = UUID.fromString(resultSet.getString("medication_id"))
                        )
                    } else {
                        throw NoSuchElementException("No medication found for id.")
                    }
                }
            }
        }
    }

    // 3. Return user_medication by user_id and medication_id with the least date of creation.
    suspend fun getUserMedicationByUmAndUserIds(form: GetMedicationForm, secretKey: SecretKey): UserMedication =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement(SqlQueriesUserMedication.GET_USER_MEDICATION_BY_UID_AND_UMID)
                    .use { statement ->
                        statement.setString(1, form.userId)
                        statement.setString(2, form.medicationId)

                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                val dosage = decryptField(
                                    resultSet.getString("dosage_encrypted"),
                                    resultSet.getString("dosage_iv"),
                                    secretKey
                                )
                                val frequency = decryptField(
                                    resultSet.getString("frequency_encrypted"),
                                    resultSet.getString("frequency_iv"),
                                    secretKey
                                )
                                val notes = decryptField(
                                    resultSet.getString("notes_encrypted"),
                                    resultSet.getString("notes_iv"),
                                    secretKey
                                )
                                return@withContext UserMedication(
                                    id = UUID.fromString(resultSet.getString("id")),
                                    userId = UUID.fromString(resultSet.getString("user_id")),
                                    dosage = dosage,
                                    frequency = frequency,
                                    startDate = resultSet.getTimestamp("start_date"),
                                    endDate = resultSet.getTimestamp("end_date"),
                                    notes = notes,
                                    medicationName = resultSet.getString("name"),
                                    description = resultSet.getString("description"),
                                    manufacturer = resultSet.getString("manufacturer"),
                                    form = resultSet.getString("form"),
                                    strength = resultSet.getString("strength"),
                                    medicationId = UUID.fromString(resultSet.getString("medication_id"))
                                )
                            } else {
                                throw NoSuchElementException(
                                    "No medication found for userId=${form.userId} and medicationId=${form.medicationId}"
                                )
                            }
                        }
                    }
            }
        }

    // 4. Returns list of user_medication for actual date and when it is not deleted. Not needed soon.
    suspend fun getTodayUserMedicationByUserId(userId: String, secretKey: SecretKey): List<UserMedication> = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueriesUserMedication.GET_TODAY_MEDICATION_BY_USER_ID).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { resultSet ->
                    val todayMedications = mutableListOf<UserMedication>()

                    while (resultSet.next()) {
                        val dosage = decryptField(
                            resultSet.getString("dosage_encrypted"),
                            resultSet.getString("dosage_iv"),
                            secretKey
                        )
                        val frequency = decryptField(
                            resultSet.getString("frequency_encrypted"),
                            resultSet.getString("frequency_iv"),
                            secretKey
                        )
                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )
                        todayMedications.add(
                            UserMedication(
                                id = UUID.fromString(resultSet.getString("id")),
                                userId = UUID.fromString(resultSet.getString("user_id")),
                                dosage = dosage,
                                frequency = frequency,
                                startDate = resultSet.getTimestamp("start_date"),
                                endDate = resultSet.getTimestamp("end_date"),
                                notes = notes,
                                medicationName = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                manufacturer = resultSet.getString("manufacturer"),
                                form = resultSet.getString("form"),
                                strength = resultSet.getString("strength"),
                                medicationId = UUID.fromString(resultSet.getString("medication_id"))
                            )
                        )
                    }
                    return@withContext todayMedications
                }
            }
        }
    }

    // 5. Returns history of all not hard deleted user medications.
    suspend fun getUserMedicationHistory(userId: String, secretKey: SecretKey): List<UserMedication> = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueriesUserMedication.GET_USER_MEDICATION_HISTORY).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { resultSet ->
                    val medicationHistory = mutableListOf<UserMedication>()

                    while (resultSet.next()) {
                        val dosage = decryptField(
                            resultSet.getString("dosage_encrypted"),
                            resultSet.getString("dosage_iv"),
                            secretKey
                        )
                        val frequency = decryptField(
                            resultSet.getString("frequency_encrypted"),
                            resultSet.getString("frequency_iv"),
                            secretKey
                        )
                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )
                        medicationHistory.add(
                            UserMedication(
                                id = UUID.fromString(resultSet.getString("id")),
                                userId = UUID.fromString(resultSet.getString("user_id")),
                                dosage = dosage,
                                frequency = frequency,
                                startDate = resultSet.getTimestamp("start_date"),
                                endDate = resultSet.getTimestamp("end_date"),
                                notes = notes,
                                medicationName = resultSet.getString("name"),
                                description = resultSet.getString("description"),
                                manufacturer = resultSet.getString("manufacturer"),
                                form = resultSet.getString("form"),
                                strength = resultSet.getString("strength"),
                                medicationId = UUID.fromString(resultSet.getString("medication_id"))
                            )
                        )
                    }
                    return@withContext medicationHistory
                }
            }
        }
    }

    // 6. Returns user_medication.id for a pair of a user id and a medication id.
    suspend fun getUserMedicationId(id: String, medicationId: String): String = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueriesUserMedication.GET_USER_MEDICATION_ID).use { statement ->
                statement.setString(1, id)
                statement.setString(2, medicationId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext (resultSet.getString("id"))
                    } else {
                        throw NoSuchElementException("No medication found for id.")
                    }
                }
            }
        }
    }

    // Post/Put Data methods of User Medication
    // 1.  Softly delete UM for user, 2. Softly delete UM for UM.ID 3. Mark synced

    // 1. Soft Delete user_medication by user_id (all um for u)
    suspend fun deleteUserMedicationByUserId(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery =
            "UPDATE glucoconnectapi.user_medications SET is_deleted = ?, last_updated_on=? WHERE user_id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.apply {
                    setBoolean(1, true)
                    setTimestamp(2, Timestamp(System.currentTimeMillis()))
                    setString(3, id)
                }
                statement.executeUpdate()
            }
        }
    }

    // 2. Soft Delete user_medication by user_medication.id
    suspend fun deleteUserMedicationById(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery =
            "UPDATE glucoconnectapi.user_medications SET is_deleted = ?, last_updated_on=? WHERE  user_medications.id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.apply {
                    setBoolean(1, true)
                    setTimestamp(2, Timestamp(System.currentTimeMillis()))
                    setString(3, id)
                }
                statement.executeUpdate()
            }
        }
    }

    // 3. Update is_synced to TRUE value for all user_medications
    suspend fun markAsSynced(userId: String) = withContext(Dispatchers.IO) {
        val blockUserQuery = """UPDATE glucoconnectapi.user_medications
SET is_synced = ?, last_updated_on =?
WHERE user_id = ?;"""

        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement(blockUserQuery).use { statement ->
                    statement.apply {
                        setBoolean(1, true)
                        setTimestamp(2, Timestamp(System.currentTimeMillis()))
                        setString(3, userId)
                    }
                    statement.executeUpdate()
                }
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            }
        }
    }
}
