package infrastructure

import decryptField
import encryptField
import form.HeartbeatForm
import form.HeartbeatReturn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.*
import javax.crypto.SecretKey
import javax.sql.DataSource

class HeartbeatResultDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS glucoconnectapi.heartbeat_measurements (
            id CHAR(36) PRIMARY KEY NOT NULL,
            user_id CHAR(36) NOT NULL,
            timestamp TIMESTAMP NOT NULL,
            systolic_pressure_encrypted TEXT NOT NULL,
            systolic_pressure_iv TEXT NOT NULL,
            diastolic_pressure_encrypted TEXT NOT NULL,
            diastolic_pressure_iv TEXT NOT NULL,
            pulse_encrypted TEXT NOT NULL,
            pulse_iv TEXT NOT NULL,
            note_encrypted TEXT NOT NULL,
            note_iv TEXT NOT NULL,
            last_updated_on TIMESTAMP,
            is_deleted BOOLEAN DEFAULT FALSE,
            is_synced BOOLEAN DEFAULT TRUE
            )
        """

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                try {
                    statement.execute(createTableQuery)
                } catch (e: SQLException) {
                    if (!e.message?.contains("already exists")!!) {
                        throw e
                    } else {
                        // znwou tego wymaga
                    }
                }

            }
        }
    }

    suspend fun createHeartbeatResult(form: HeartbeatForm, secretKey: SecretKey): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val createHeartbeatResultQuery = """
            INSERT INTO glucoconnectapi.heartbeat_measurements (id, user_id, timestamp, systolic_pressure_encrypted, systolic_pressure_iv,
             diastolic_pressure_encrypted, diastolic_pressure_iv, pulse_encrypted, pulse_iv, note_encrypted, note_iv)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
        """

        val (systolicPressureEncrypted, systolicPressureIv) = encryptField(form.systolicPressure.toString(), secretKey)
        val (diastolicPressureEncrypted, diastolicPressureIv) = encryptField(
            form.diastolicPressure.toString(),
            secretKey
        )
        val (pulseEncrypted, pulseIv) = encryptField(form.pulse.toString(), secretKey)
        val (noteEncrypted, noteIv) = encryptField(form.note, secretKey)

        dataSource.connection.use { connection ->
            connection.prepareStatement(createHeartbeatResultQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, form.userId.toString())
                    setTimestamp(3, Timestamp(form.timestamp.time))
                    setString(4, systolicPressureEncrypted)
                    setString(5, systolicPressureIv)
                    setString(6, diastolicPressureEncrypted)
                    setString(7, diastolicPressureIv)
                    setString(8, pulseEncrypted)
                    setString(9, pulseIv)
                    setString(10, noteEncrypted)
                    setString(11, noteIv)
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

    suspend fun readById(id: String, secretKey: SecretKey): HeartbeatReturn = withContext(Dispatchers.IO) {
        val selectQuery = """
            SELECT id, user_id, timestamp, systolic_pressure_encrypted, systolic_pressure_iv, diastolic_pressure_encrypted, diastolic_pressure_iv, pulse_encrypted, pulse_iv, note_encrypted, note_iv FROM heartbeat_measurements 
            WHERE id = ?
        """
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val systolicPressure = decryptField(
                            resultSet.getString("systolic_pressure_encrypted"),
                            resultSet.getString("systolic_pressure_iv"),
                            secretKey
                        ).toInt()
                        val diastolicPressure = decryptField(
                            resultSet.getString("diastolic_pressure_encrypted"),
                            resultSet.getString("diastolic_pressure_iv"),
                            secretKey
                        ).toInt()
                        val pulse = decryptField(
                            resultSet.getString("pulse_encrypted"),
                            resultSet.getString("pulse_iv"),
                            secretKey
                        ).toInt()
                        val note = decryptField(
                            resultSet.getString("note_encrypted"),
                            resultSet.getString("note_iv"),
                            secretKey
                        )


                        return@withContext HeartbeatReturn(
                            UUID.fromString(resultSet.getString("id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            resultSet.getTimestamp("timestamp"),
                            systolicPressure,
                            diastolicPressure,
                            pulse,
                            note
                        )
                    } else {
                        throw NoSuchElementException("No such element")
                    }

                }
            }
        }

    }


    suspend fun getHeartbeatByUserId(id: String, secretKey: SecretKey): List<HeartbeatReturn> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<HeartbeatReturn>()
            val selectQuery = "SELECT * FROM heartbeat_measurements WHERE user_id = ? ORDER BY timestamp DESC\n" +
                    "LIMIT 100"

            dataSource.connection.use { connection ->
                connection.prepareStatement(selectQuery).use { statement ->
                    statement.setString(1, id)
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val systolicPressure = decryptField(
                                resultSet.getString("systolic_pressure_encrypted"),
                                resultSet.getString("systolic_pressure_iv"),
                                secretKey
                            ).toInt()
                            val diastolicPressure = decryptField(
                                resultSet.getString("diastolic_pressure_encrypted"),
                                resultSet.getString("diastolic_pressure_iv"),
                                secretKey
                            ).toInt()
                            val pulse = decryptField(
                                resultSet.getString("pulse_encrypted"),
                                resultSet.getString("pulse_iv"),
                                secretKey
                            ).toInt()
                            val note = decryptField(
                                resultSet.getString("note_encrypted"),
                                resultSet.getString("note_iv"),
                                secretKey
                            )
                            results.add(
                                HeartbeatReturn(
                                    UUID.fromString(resultSet.getString("id")),
                                    UUID.fromString(resultSet.getString("user_id")),
                                    resultSet.getTimestamp("timestamp"),
                                    systolicPressure,
                                    diastolicPressure,
                                    pulse,
                                    note
                                )
                            )
                        }
                    }
                }
            }
            return@withContext results
        }

    suspend fun getThreeHeartbeatResults(id: String, secretKey: SecretKey): List<HeartbeatReturn> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<HeartbeatReturn>()
            val selectQuery = "SELECT * FROM heartbeat_measurements WHERE user_id = ? ORDER BY timestamp DESC\n" +
                    "LIMIT 3"

            dataSource.connection.use { connection ->
                connection.prepareStatement(selectQuery).use { statement ->
                    statement.setString(1, id)
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val systolicPressure = decryptField(
                                resultSet.getString("systolic_pressure_encrypted"),
                                resultSet.getString("systolic_pressure_iv"),
                                secretKey
                            ).toInt()
                            val diastolicPressure = decryptField(
                                resultSet.getString("diastolic_pressure_encrypted"),
                                resultSet.getString("diastolic_pressure_iv"),
                                secretKey
                            ).toInt()
                            val pulse = decryptField(
                                resultSet.getString("pulse_encrypted"),
                                resultSet.getString("pulse_iv"),
                                secretKey
                            ).toInt()
                            val note = decryptField(
                                resultSet.getString("note_encrypted"),
                                resultSet.getString("note_iv"),
                                secretKey
                            )
                            results.add(
                                HeartbeatReturn(
                                    UUID.fromString(resultSet.getString("id")),
                                    UUID.fromString(resultSet.getString("user_id")),
                                    resultSet.getTimestamp("timestamp"),
                                    systolicPressure,
                                    diastolicPressure,
                                    pulse,
                                    note
                                )
                            )
                        }
                    }
                }
            }
            return@withContext results
        }

    suspend fun deleteResult(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery = "DELETE FROM heartbeat_measurements WHERE id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    suspend fun deleteResultsForUser(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery = "DELETE FROM heartbeat_measurements WHERE user_id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }


}