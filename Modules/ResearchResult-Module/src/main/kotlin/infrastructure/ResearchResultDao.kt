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
            CREATE TABLE IF NOT EXISTS public.glucose_measurements (
            id CHAR(36) PRIMARY KEY,
            sequenceNumber INT NOT NULL,
            glucoseConcentration DOUBLE PRECISION NOT NULL,
            unit VARCHAR(30) NOT NULL,
            timestamp TIMESTAMP NOT NULL,
            userId CHAR(36), 
    deletedOn TIMESTAMP,
    lastUpdatedOn TIMESTAMP
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
        INSERT INTO public.glucosemeasurements (id, sequenceNumber, glucoseConcentration, unit, timestamp, userid)
        VALUES (?, ?, ?, ?, ?, ?);
    """
        dataSource.connection.use { connection ->
            connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS).use {  statement ->
                statement.apply {
                    setString(1, id.toString())
                    setInt(2, form.sequenceNumber)
                    setDouble(3, form.glucoseConcentration)
                    setString(4, form.unit)
                    setTimestamp(5, Timestamp(form.timestamp.time))
                    setString(6, form.userId.toString())
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

    suspend fun sync(result: ResearchResult): ResearchResult = withContext(Dispatchers.IO) {
        // Sprawdź, czy rekord istnieje na serwerze
        val query = """
        SELECT id FROM public.glucosemeasurements WHERE id = ?;
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
            UPDATE public.glucosemeasurements 
            SET sequenceNumber = ?, glucoseConcentration = ?, unit = ?, timestamp = ?, userid = ? 
            WHERE id = ?;
        """

            dataSource.connection.use { connection ->
                connection.prepareStatement(updateQuery).use { statement ->
                    statement.apply {
                        setInt(1, result.sequenceNumber)
                        setDouble(2, result.glucoseConcentration)
                        setString(3, result.unit)
                        setTimestamp(4, Timestamp(result.timestamp.time))
                        setString(5, result.userId.toString())
                        setString(6, result.id.toString())
                    }
                    statement.executeUpdate()
                }
            }
        } else {
            // Rekord nie istnieje na serwerze - dodaj go
            val insertQuery = """
            INSERT INTO public.glucosemeasurements (id, sequenceNumber, glucoseConcentration, unit, timestamp, userid)
            VALUES (?, ?, ?, ?, ?, ?);
        """
            dataSource.connection.use { connection ->
                connection.prepareStatement(insertQuery).use { statement ->
                    statement.apply {
                        setString(1, result.id.toString())
                        setInt(2, result.sequenceNumber)
                        setDouble(3, result.glucoseConcentration)
                        setString(4, result.unit)
                        setTimestamp(5, Timestamp(result.timestamp.time))
                        setString(6, result.userId.toString())
                    }
                    statement.executeUpdate()
                }
            }
        }

        // Zwróć zaktualizowany obiekt
        return@withContext result
    }


    suspend fun read(id: String): ResearchResult = withContext(Dispatchers.IO) {
        val selectQuery = """
            SELECT id, sequenceNumber, glucoseConcentration, unit, timestamp, userId, deletedOn, lastUpdatedOn 
            FROM public.glucosemeasurements
            WHERE id = ?
        """
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext ResearchResult(
                            UUID.fromString(resultSet.getString("id")),
                            resultSet.getInt("sequenceNumber"),
                            resultSet.getDouble("glucoseConcentration"),
                            resultSet.getString("unit")?.let { PrefUnitType.valueOf(it)}.toString(),
                            resultSet.getTimestamp("timestamp"),
                            resultSet.getString("userId")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                            resultSet.getTimestamp("deletedOn"),
                            resultSet.getTimestamp("lastUpdatedOn")
                        )
                    } else {
                        throw NoSuchElementException("Record with ID $id not found")
                    }
                }
            }
        }
    }

    suspend fun getAll(): List<ResearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ResearchResult>()
        val selectAllQuery = "SELECT * FROM public.glucosemeasurements"

        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(
                            ResearchResult(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getInt("sequenceNumber"),
                                resultSet.getDouble("glucoseConcentration"),
                                resultSet.getString("unit")?.let { PrefUnitType.valueOf(it)}.toString(),
                                resultSet.getTimestamp("timestamp"),
                                resultSet.getString("userId")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                                resultSet.getTimestamp("deletedOn"),
                                resultSet.getTimestamp("lastUpdatedOn")
                            )
                        )
                    }
                }
            }
        }
        return@withContext results
    }

    suspend fun getThreeResultsForUser(id: String): List<ResearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ResearchResult>()
        val selectAllQuery = """SELECT * FROM public.glucosemeasurements WHERE deletedOn IS NULL AND userId = ?
ORDER BY timestamp DESC
LIMIT 3;
"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(
                            ResearchResult(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getInt("sequenceNumber"),
                                resultSet.getDouble("glucoseConcentration"),
                                resultSet.getString("unit")?.let { PrefUnitType.valueOf(it)}.toString(),
                                resultSet.getTimestamp("timestamp"),
                                resultSet.getString("userId")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                                resultSet.getTimestamp("deletedOn"),
                                resultSet.getTimestamp("lastUpdatedOn")
                            )
                        )
                    }
                }
            }
        }
        return@withContext results
    }

    suspend fun getResultsByUserId(id: String): List<ResearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ResearchResult>()
        val selectAllQuery = """SELECT * FROM public.glucosemeasurements WHERE deletedOn IS NULL AND userId = ?
ORDER BY timestamp DESC
LIMIT 100;
"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        results.add(
                            ResearchResult(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getInt("sequenceNumber"),
                                resultSet.getDouble("glucoseConcentration"),
                                resultSet.getString("unit")?.let { PrefUnitType.valueOf(it)}.toString(),
                                resultSet.getTimestamp("timestamp"),
                                resultSet.getString("userId")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                                resultSet.getTimestamp("deletedOn"),
                                resultSet.getTimestamp("lastUpdatedOn")
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
            UPDATE public.glucosemeasurements 
            SET sequenceNumber = ?, 
                glucoseConcentration = ?, 
                unit = ?, 
                timestamp = ? ,
                lastUpdatedOn = ?
            WHERE id = ?
        """

        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(updateQuery).use { statement ->
                    statement.apply {
                        setInt(1, form.sequenceNumber)
                        setDouble(2, form.glucoseConcentration)
                        setString(3, form.unit)
                        setTimestamp(4, form.timestamp?.let { Timestamp(it.time) })
                        setTimestamp(5, Timestamp(System.currentTimeMillis()))
                        setString(6, form.id.toString())
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
        val deleteQuery = "DELETE FROM public.glucosemeasurements WHERE id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    suspend fun safeDeleteResult(form: SafeDeleteResultForm) = withContext(Dispatchers.IO) {
        val safeDeleteQuery = """UPDATE public.glucosemeasurements
                SET deletedOn = ?
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
