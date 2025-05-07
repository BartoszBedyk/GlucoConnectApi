package infrastructure

import form.HeartbeatForm
import form.HeartbeatReturn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.*
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
            systolic_pressure INT NOT NULL,
            diastolic_pressure INT NOT NULL,
            pulse INT NOT NULL,
            note TEXT NOT NULL
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

    suspend fun createHeartbeatResult(form: HeartbeatForm): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val createHeartbeatResultQuery = """
            INSERT INTO glucoconnectapi.heartbeat_measurements (id, user_id, timestamp, systolic_pressure, diastolic_pressure, pulse, note)
            VALUES (?,?,?,?,?,?,?)
        """

        dataSource.connection.use { connection ->
            connection.prepareStatement(createHeartbeatResultQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, form.userId.toString())
                    setTimestamp(3, Timestamp(form.timestamp.time))
                    setInt(4, form.systolicPressure)
                    setInt(5, form.diastolicPressure)
                    setInt(6, form.pulse)
                    setString(7, form.note)

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

    suspend fun readById(id: String): HeartbeatReturn = withContext(Dispatchers.IO) {
        val selectQuery = """
            SELECT id, user_id, timestamp, systolic_pressure, diastolic_pressure, pulse, note FROM heartbeat_measurements 
            WHERE id = ?
        """
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext HeartbeatReturn(
                            UUID.fromString(resultSet.getString("id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            resultSet.getTimestamp("timestamp"),
                            resultSet.getInt("systolic_pressure"),
                            resultSet.getInt("diastolic_pressure"),
                            resultSet.getInt("pulse"),
                            resultSet.getString("note")
                        )
                    } else {
                        throw NoSuchElementException("No such element")
                    }

                }
            }
        }

    }



    suspend fun getHeartbeatByUserId(id: String): List<HeartbeatReturn> = withContext(Dispatchers.IO) {
        val results = mutableListOf<HeartbeatReturn>()
        val selectQuery = "SELECT * FROM heartbeat_measurements WHERE user_id = ? ORDER BY timestamp DESC\n" +
                "LIMIT 100"

        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(
                            HeartbeatReturn(
                                UUID.fromString(resultSet.getString("id")),
                                UUID.fromString(resultSet.getString("user_id")),
                                resultSet.getTimestamp("timestamp"),
                                resultSet.getInt("systolic_pressure"),
                                resultSet.getInt("diastolic_pressure"),
                                resultSet.getInt("pulse"),
                                resultSet.getString("note")
                            )
                        )
                    }
                }
            }
        }
        return@withContext results
    }

    suspend fun getThreeHeartbeatResults(id: String): List<HeartbeatReturn> = withContext(Dispatchers.IO) {
        val results = mutableListOf<HeartbeatReturn>()
        val selectQuery = "SELECT * FROM heartbeat_measurements WHERE user_id = ? ORDER BY timestamp DESC\n" +
                "LIMIT 3"

        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(
                            HeartbeatReturn(
                                UUID.fromString(resultSet.getString("id")),
                                UUID.fromString(resultSet.getString("user_id")),
                                resultSet.getTimestamp("timestamp"),
                                resultSet.getInt("systolic_pressure"),
                                resultSet.getInt("diastolic_pressure"),
                                resultSet.getInt("pulse"),
                                resultSet.getString("note")
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