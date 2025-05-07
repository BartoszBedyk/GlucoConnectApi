package infrastructure

import form.CreateObserver
import form.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

class ObserverDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """CREATE TABLE IF NOT EXISTS glucoconnectapi.observers (
    id CHAR(36) PRIMARY KEY,
    observer_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.users(id) ON DELETE CASCADE,
    observed_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.users(id) ON DELETE CASCADE,
    is_accepted BOOLEAN DEFAULT FALSE
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


    suspend fun observe(createForm: CreateObserver): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val creteQuery = """INSERT INTO glucoconnectapi.observers(id, observer_id, observed_id) VALUES (?,?,?) """

        dataSource.connection.use { connection ->
            connection.prepareStatement(creteQuery, Statement.RETURN_GENERATED_KEYS)
                .use { statement ->
                    statement.apply {
                        setString(1, id.toString())
                        setString(2, createForm.observerId)
                        setString(3, createForm.observedId)
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

    suspend fun getObservedAcceptedByObserverId(observerId: String): List<Observer> = withContext(Dispatchers.IO) {
        val getObservedQuery = """ SELECT * FROM glucoconnectapi.observers WHERE observer_id = ? AND is_accepted = ?"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(getObservedQuery).use { statement ->
                statement.setString(1, observerId)
                statement.setBoolean(2, true)
                statement.executeQuery().use { resultSet ->
                    val todayMedications = mutableListOf<Observer>()

                    while (resultSet.next()) {
                        todayMedications.add(
                            Observer(
                                UUID.fromString(resultSet.getString("id")),
                                UUID.fromString(resultSet.getString("observer_id")),
                                UUID.fromString(resultSet.getString("observed_id")),
                                resultSet.getBoolean("is_accepted")
                            )
                        )
                    }
                    return@withContext todayMedications
                }
            }
        }
    }

    suspend fun getObservedUnAcceptedByObserverId(observerId: String): List<Observer> = withContext(Dispatchers.IO) {
        val getObservedQuery = """ SELECT * FROM glucoconnectapi.observers WHERE observer_id = ? AND is_accepted = ?"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(getObservedQuery).use { statement ->
                statement.setString(1, observerId)
                statement.setBoolean(2, false)
                statement.executeQuery().use { resultSet ->
                    val todayMedications = mutableListOf<Observer>()

                    while (resultSet.next()) {
                        todayMedications.add(
                            Observer(
                                UUID.fromString(resultSet.getString("id")),
                                UUID.fromString(resultSet.getString("observer_id")),
                                UUID.fromString(resultSet.getString("observed_id")),
                                resultSet.getBoolean("is_accepted")
                            )
                        )
                    }
                    return@withContext todayMedications
                }
            }
        }
    }

    suspend fun getObservatorsByObservedIdAccepted(observedId: String): List<Observer> = withContext(Dispatchers.IO) {
        val getObservedQuery = """ SELECT * FROM glucoconnectapi.observers WHERE observed_id = ? AND is_accepted = ?"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(getObservedQuery).use { statement ->
                statement.setString(1, observedId)
                statement.setBoolean(2, true)
                statement.executeQuery().use { resultSet ->
                    val todayMedications = mutableListOf<Observer>()

                    while (resultSet.next()) {
                        todayMedications.add(
                            Observer(
                                UUID.fromString(resultSet.getString("id")),
                                UUID.fromString(resultSet.getString("observer_id")),
                                UUID.fromString(resultSet.getString("observed_id")),
                                resultSet.getBoolean("is_accepted")
                            )
                        )
                    }
                    println("Accepted results UnAccepted: $todayMedications")
                    return@withContext todayMedications
                }
            }
        }
    }

    suspend fun getObservatorsByObservedIdUnAccepted(observedId: String): List<Observer> = withContext(Dispatchers.IO) {
        val getObservedQuery = """ SELECT * FROM glucoconnectapi.observers WHERE observed_id = ? AND is_accepted = ?"""
        dataSource.connection.use { connection ->
            connection.prepareStatement(getObservedQuery).use { statement ->
                statement.setString(1, observedId)
                statement.setBoolean(2, false)
                statement.executeQuery().use { resultSet ->
                    val todayMedications = mutableListOf<Observer>()

                    while (resultSet.next()) {
                        todayMedications.add(
                            Observer(
                                UUID.fromString(resultSet.getString("id")),
                                UUID.fromString(resultSet.getString("observer_id")),
                                UUID.fromString(resultSet.getString("observed_id")),
                                resultSet.getBoolean("is_accepted")
                            )
                        )
                    }
                    println("Accepted results UnAccepted: $todayMedications")
                    return@withContext todayMedications
                }
            }
        }
    }


    suspend fun acceptObservation(createObserver: CreateObserver): Int = withContext(Dispatchers.IO) {
        val query = """UPDATE glucoconnectapi.observers
                   SET is_accepted = ?
                   WHERE observer_id = ? AND observed_id = ?"""

        dataSource.connection.use { connection ->
            try {
                connection.autoCommit = false

                val updatedRows = connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, true)
                    statement.setString(2, createObserver.observerId)
                    statement.setString(3, createObserver.observedId)
                    statement.executeUpdate()
                }

                connection.commit()
                updatedRows
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            } finally {
                connection.autoCommit = true
            }
        }
    }



    suspend fun unAcceptObservation(createObserver: CreateObserver): Int = withContext(Dispatchers.IO) {
        val query = """UPDATE glucoconnectapi.observers
                   SET is_accepted = ?
                   WHERE observer_id = ? AND observed_id = ?"""

        dataSource.connection.use { connection ->
            try {
                connection.autoCommit = false

                val updatedRows = connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, false)
                    statement.setString(2, createObserver.observerId)
                    statement.setString(3, createObserver.observedId)
                    statement.executeUpdate()
                }

                connection.commit()
                updatedRows
            } catch (ex: Exception) {
                connection.rollback()
                throw ex
            } finally {
                connection.autoCommit = true
            }
        }
    }


}