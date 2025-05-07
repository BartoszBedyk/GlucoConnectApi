package infrastructure

import form.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

class ResearchResultDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS glucoconnectapi.glucose_measurements (
            id CHAR(36) PRIMARY KEY,
            glucose_concentration DOUBLE PRECISION NOT NULL,
            unit VARCHAR(30) NOT NULL,
            timestamp TIMESTAMP NOT NULL,
            empty_stomach BOOLEAN,
            after_medication BOOLEAN,
            notes TEXT,
            user_id CHAR(36), 
    deleted_on TIMESTAMP,
    last_updated_on TIMESTAMP
    CHECK (unit IN ('MG_PER_DL', 'MMOL_PER_L')));
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

    suspend fun create(form: ResearchResultForm): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val insertQuery = """
        INSERT INTO glucoconnectapi.glucose_measurements (id, glucose_concentration, unit, timestamp, after_medication, empty_stomach, notes, user_id)
        VALUES (?, ?, ?, ?, ?, ?, ?,?);
    """
        dataSource.connection.use { connection ->
            connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setDouble(2, form.glucoseConcentration)
                    setString(3, form.unit)
                    setTimestamp(4, Timestamp(form.timestamp.time))
                    setBoolean(5, form.afterMedication)
                    setBoolean(6, form.emptyStomach)
                    setString(7, form.notes)
                    setString(8, form.userId.toString())
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

    suspend fun sync(result: GlucoseResult): GlucoseResult = withContext(Dispatchers.IO) {
        // Sprawdź, czy rekord istnieje na serwerze
        val query = """
        SELECT id FROM glucoconnectapi.glucose_measurements WHERE id = ?;
    """

        val existsOnServer = dataSource.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, result.id.toString())
                statement.executeQuery().use { resultSet ->
                    resultSet.next() // Zwróci true, jeśli rekord istnieje
                }
            }
        }

        if (existsOnServer) {
            // Aktualizuj rekord na serwerze
            val updateQuery = """
            UPDATE glucoconnectapi.glucose_measurements 
            SET  glucose_concentration = ?, unit = ?, timestamp = ?, after_medication = ?, empty_stomach= ?, notes = ?, user_id = ? 
            WHERE id = ?;
        """

            dataSource.connection.use { connection ->
                connection.prepareStatement(updateQuery).use { statement ->
                    statement.apply {
                        setDouble(1, result.glucoseConcentration)
                        setString(2, result.unit)
                        setTimestamp(3, Timestamp(result.timestamp.time))
                        setBoolean(4, result.afterMedication)
                        setBoolean(5, result.emptyStomach)
                        setString(6, result.notes)
                        setString(7, result.userId.toString())
                    }
                    statement.executeUpdate()
                }
            }
        } else {
            // Rekord nie istnieje na serwerze - dodaj go
            val insertQuery = """
            INSERT INTO glucoconnectapi.glucose_measurements (id, glucose_concentration, unit, timestamp, after_medication, empty_stomach, notes, user_id)
            VALUES (?, ?, ?, ?, ?, ?,?,?);
        """
            dataSource.connection.use { connection ->
                connection.prepareStatement(insertQuery).use { statement ->
                    statement.apply {
                        setString(1, result.id.toString())
                        setDouble(2, result.glucoseConcentration)
                        setString(3, result.unit)
                        setTimestamp(4, Timestamp(result.timestamp.time))
                        setBoolean(5, result.afterMedication)
                        setBoolean(6, result.emptyStomach)
                        setString(7, result.notes)
                        setString(8, result.userId.toString())
                    }
                    statement.executeUpdate()
                }
            }
        }

        // Zwróć zaktualizowany obiekt
        return@withContext result
    }


    suspend fun getGlucoseResultById(id: String): GlucoseResult = withContext(Dispatchers.IO) {
        val selectQuery = """
            SELECT id, glucose_concentration, unit, timestamp, user_id, deleted_on, last_updated_on, after_medication, empty_stomach, notes 
            FROM glucoconnectapi.glucose_measurements
            WHERE id = ?
        """
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext GlucoseResult(UUID.fromString(resultSet.getString("id")),
                            resultSet.getDouble("glucose_concentration"),
                            resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            resultSet.getTimestamp("timestamp"),
                            resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                            resultSet.getTimestamp("deleted_on"),
                            resultSet.getTimestamp("last_updated_on"),
                            resultSet.getBoolean("after_medication"),
                            resultSet.getBoolean("empty_stomach"),
                            resultSet.getString("notes")

                        )
                    } else {
                        throw NoSuchElementException("Record with ID $id not found")
                    }
                }
            }
        }
    }

    suspend fun getAll(): List<GlucoseResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GlucoseResult>()
        val selectAllQuery = "SELECT * FROM glucoconnectapi.glucose_measurements"

        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(GlucoseResult(UUID.fromString(resultSet.getString("id")),
                            resultSet.getDouble("glucose_concentration"),
                            resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            resultSet.getTimestamp("timestamp"),
                            resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                            resultSet.getTimestamp("deleted_on"),
                            resultSet.getTimestamp("last_updated_on"),
                            resultSet.getBoolean("after_medication"),
                            resultSet.getBoolean("empty_stomach"),
                            resultSet.getString("notes")
                        )
                        )
                    }
                }
            }
        }
        return@withContext results
    }

    suspend fun getThreeResultsForUser(id: String): List<GlucoseResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GlucoseResult>()
        val selectAllQuery =
            """SELECT * FROM glucoconnectapi.glucose_measurements WHERE deleted_on IS NULL AND user_id = ?
ORDER BY timestamp DESC
LIMIT 3;
"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(GlucoseResult(UUID.fromString(resultSet.getString("id")),
                            resultSet.getDouble("glucose_concentration"),
                            resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            resultSet.getTimestamp("timestamp"),
                            resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                            resultSet.getTimestamp("deleted_on"),
                            resultSet.getTimestamp("last_updated_on"),
                            resultSet.getBoolean("after_medication"),
                            resultSet.getBoolean("empty_stomach"),
                            resultSet.getString("notes")
                        )
                        )
                    }
                }
            }
        }
        return@withContext results
    }

    suspend fun getResultsByUserId(id: String): List<GlucoseResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GlucoseResult>()
        val selectAllQuery =
            """SELECT * FROM glucoconnectapi.glucose_measurements WHERE deleted_on IS NULL AND user_id = ?
ORDER BY timestamp DESC
LIMIT 100;
"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(GlucoseResult(UUID.fromString(resultSet.getString("id")),
                            resultSet.getDouble("glucose_concentration"),
                            resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            resultSet.getTimestamp("timestamp"),
                            resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                            resultSet.getTimestamp("deleted_on"),
                            resultSet.getTimestamp("last_updated_on"),
                            resultSet.getBoolean("after_medication"),
                            resultSet.getBoolean("empty_stomach"),
                            resultSet.getString("notes")
                        )
                        )
                    }
                }
            }
        }
        return@withContext results
    }


    suspend fun updateResult(form: UpdateResearchResultForm) = withContext(Dispatchers.IO) {
        val updateQuery = """
            UPDATE glucoconnectapi.glucose_measurements 
            SET 
                glucose_concentration = ?, 
                unit = ?, 
                timestamp = ? ,
                last_updated_on = ?,
                after_medication = ?,
                empty_stomach = ?,
                notes = ?
            WHERE id = ?
        """

        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(updateQuery).use { statement ->
                    statement.apply {
                        setDouble(1, form.glucoseConcentration)
                        setString(2, form.unit)
                        setTimestamp(3, form.timestamp?.let { Timestamp(it.time) })
                        setTimestamp(4, Timestamp(System.currentTimeMillis()))
                        setBoolean(5, form.afterMedication)
                        setBoolean(6, form.emptyStomach)
                        setString(7, form.notes)
                        setString(8, form.id.toString())
                    }
                    statement.executeUpdate()
                    connection.commit()
                }
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun deleteResult(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery = "DELETE FROM glucoconnectapi.glucose_measurements WHERE id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    suspend fun safeDeleteResult(form: SafeDeleteResultForm) = withContext(Dispatchers.IO) {
        val safeDeleteQuery = """UPDATE glucoconnectapi.glucose_measurements
                SET deleted_on = ?
                WHERE id = ?"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(safeDeleteQuery).use { statement ->
                statement.setTimestamp(1, Timestamp(System.currentTimeMillis()))
                statement.setString(2, form.id.toString())
                statement.executeUpdate()
            }
        }
    }
}
