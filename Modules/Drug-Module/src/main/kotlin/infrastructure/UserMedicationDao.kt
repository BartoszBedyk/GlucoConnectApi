package infrastructure

import DateSerializer
import form.CreateUserMedication
import form.GetMedicationForm
import form.Medication
import form.UserMedication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

class UserMedicationDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """CREATE TABLE IF NOT EXISTS glucoconnectapi.user_medications (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.users(id) ON DELETE CASCADE,
    medication_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.medications(id) ON DELETE CASCADE,
    dosage VARCHAR(50),
    frequency VARCHAR(50),
    start_date DATE,
    end_date DATE,
    notes TEXT,
    is_synced BOOLEAN DEFAULT FALSE
);
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

    suspend fun createUserMedication(createUserMedicationForm: CreateUserMedication): UUID =
        withContext(Dispatchers.IO) {
            val id: UUID = UUID.randomUUID()
            val createUserMedicationQuery = """
        INSERT INTO glucoconnectapi.user_medications (id, user_id, medication_id, dosage, frequency, start_date, end_date, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?);
    """
            dataSource.connection.use { connection ->
                connection.prepareStatement(createUserMedicationQuery, Statement.RETURN_GENERATED_KEYS)
                    .use { statement ->
                        statement.apply {
                            setString(1, id.toString())
                            setString(2, createUserMedicationForm.userId.toString())
                            setString(3, createUserMedicationForm.medicationId.toString())
                            setString(4, createUserMedicationForm.dosage)
                            setString(5, createUserMedicationForm.frequency)
                            setTimestamp(6, Timestamp(createUserMedicationForm.startDate.time))
                            setTimestamp(7, createUserMedicationForm.endDate?.let { Timestamp(it.time) })
                            setString(8, createUserMedicationForm.notes)
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


    suspend fun readUserMedication(userId: String): List<UserMedication> = withContext(Dispatchers.IO) {
        val readUserMedicationQuery = """
    SELECT 
        um.user_id, 
        um.medication_id, 
        um.dosage, 
        um.frequency, 
        um.start_date, 
        um.end_date, 
        um.notes,
        m.name,
        m.description,
        m.manufacturer,
        m.form,
        m.strength
    FROM glucoconnectapi.user_medications um
    INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
    WHERE um.user_id = ? AND (um.start_date IS NULL OR um.start_date <= CURRENT_DATE) 
    AND (um.end_date IS NULL OR um.end_date >= CURRENT_DATE)
    LIMIT 1;"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserMedicationQuery).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { resultSet ->
                    val userMedications = mutableListOf<UserMedication>()

                    while (resultSet.next()) {
                        userMedications.add(
                            UserMedication(
                                UUID.fromString(resultSet.getString("user_id")),
                                UUID.fromString(resultSet.getString("medication_id")),
                                resultSet.getString("dosage"),
                                resultSet.getString("frequency"),
                                resultSet.getTimestamp("start_date"),
                                resultSet.getTimestamp("end_date"),
                                resultSet.getString("notes"),
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                resultSet.getString("manufacturer"),
                                resultSet.getString("form"),
                                resultSet.getString("strength")
                            )
                        )
                    }
                    return@withContext userMedications
                }
            }
        }
    }

    suspend fun readUserMedicationByID(umId: String): UserMedication? = withContext(Dispatchers.IO) {
        val readUserMedicationQuery = """
        SELECT 
            um.user_id, 
            um.medication_id, 
            um.dosage, 
            um.frequency, 
            um.start_date, 
            um.end_date, 
            um.notes,
            m.name,
            m.description,
            m.manufacturer,
            m.form,
            m.strength
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
        WHERE um.id = ?;
    """
        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserMedicationQuery).use { statement ->
                statement.setString(1, umId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext UserMedication(
                            userId = UUID.fromString(resultSet.getString("user_id")),
                            medicationId = UUID.fromString(resultSet.getString("medication_id")),
                            dosage = resultSet.getString("dosage"),
                            frequency = resultSet.getString("frequency"),
                            startDate = resultSet.getTimestamp("start_date"),
                            endDate = resultSet.getTimestamp("end_date"),
                            notes = resultSet.getString("notes"),
                            medicationName = resultSet.getString("name"),
                            description = resultSet.getString("description"),
                            manufacturer = resultSet.getString("manufacturer"),
                            form = resultSet.getString("form"),
                            strength = resultSet.getString("strength")
                        )
                    } else {
                        throw NoSuchElementException("No medication found for id.")
                    }
                }
            }
        }

    }



    suspend fun readOneMedication(form: GetMedicationForm): UserMedication = withContext(Dispatchers.IO) {
        val readUserMedicationQuery = """
        SELECT 
            um.user_id, 
            um.medication_id, 
            um.dosage, 
            um.frequency, 
            um.start_date, 
            um.end_date, 
            um.notes,
            m.name,
            m.description,
            m.manufacturer,
            m.form,
            m.strength
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
        WHERE um.user_id = ? AND um.medication_id = ?;
    """

        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserMedicationQuery).use { statement ->
                statement.setString(1, form.userId.toString())
                statement.setString(2, form.medicationId.toString())

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext UserMedication(
                            userId = UUID.fromString(resultSet.getString("user_id")),
                            medicationId = UUID.fromString(resultSet.getString("medication_id")),
                            dosage = resultSet.getString("dosage"),
                            frequency = resultSet.getString("frequency"),
                            startDate = resultSet.getTimestamp("start_date"),
                            endDate = resultSet.getTimestamp("end_date"),
                            notes = resultSet.getString("notes"),
                            medicationName = resultSet.getString("name"),
                            description = resultSet.getString("description"),
                            manufacturer = resultSet.getString("manufacturer"),
                            form = resultSet.getString("form"),
                            strength = resultSet.getString("strength")
                        )
                    } else {
                        throw NoSuchElementException("No medication found for userId=${form.userId} and medicationId=${form.medicationId}")
                    }
                }
            }
        }
    }




    suspend fun readTodayUserMedication(userId: String): List<UserMedication> = withContext(Dispatchers.IO) {
        val readTodayUserMedicationQuery = """
        SELECT 
            um.user_id, 
            um.medication_id, 
            um.dosage, 
            um.frequency, 
            um.start_date, 
            um.end_date, 
            um.notes,
            m.name,
            m.description,
            m.manufacturer,
            m.form,
            m.strength
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
 WHERE um.user_id = ? 
AND (um.start_date IS NULL OR um.start_date <= CURRENT_DATE) 
AND (um.end_date IS NULL OR um.end_date >= CURRENT_DATE OR um.end_date IS NULL)


    """
        dataSource.connection.use { connection ->
            connection.prepareStatement(readTodayUserMedicationQuery).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { resultSet ->
                    val todayMedications = mutableListOf<UserMedication>()

                    while (resultSet.next()) {
                        todayMedications.add(
                            UserMedication(
                                UUID.fromString(resultSet.getString("user_id")),
                                UUID.fromString(resultSet.getString("medication_id")),
                                resultSet.getString("dosage"),
                                resultSet.getString("frequency"),
                                resultSet.getTimestamp("start_date"),
                                resultSet.getTimestamp("end_date"),
                                resultSet.getString("notes"),
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                resultSet.getString("manufacturer"),
                                resultSet.getString("form"),
                                resultSet.getString("strength")
                            )
                        )
                    }
                    return@withContext todayMedications
                }
            }
        }
    }




    suspend fun deleteUserMedication(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery = "DELETE FROM glucoconnectapi.user_medications WHERE user_id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    suspend fun deleteUserMedicationById(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery = "DELETE FROM glucoconnectapi.user_medications WHERE user_medications.id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    suspend fun getUserMedicationId(id: String, medicationId: String): String = withContext(Dispatchers.IO){
        val readUserMedicationQuery = """
        SELECT 
            um.id
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
        WHERE um.user_id = ? AND um.medication_id = ?;
    """
        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserMedicationQuery).use { statement ->
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

    suspend fun markAsSynced(userId: String) = withContext(Dispatchers.IO) {
        val blockUserQuery = """UPDATE user_medications
SET is_synced = ?
WHERE user_id = ?;"""

        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement(blockUserQuery).use { statement ->
                    statement.apply {
                        setBoolean(1, true)
                        setString(2, userId)

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

