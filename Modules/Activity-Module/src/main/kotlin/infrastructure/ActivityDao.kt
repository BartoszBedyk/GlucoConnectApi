package infrastructure

import form.Activity
import form.ActivityType
import form.CreteActivityForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.UUID

import javax.sql.DataSource

class ActivityDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                try {
                    statement.executeUpdate(SqlQueries.CREATE_ACTIVITY_TABLE)
                } catch (e: SQLException) {
                    if (!e.message?.contains("already exists")!!) {
                        throw e
                    } else {
                        // Dziala
                    }
                }
            }
        }
    }

    suspend fun createActivity(form: CreteActivityForm): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.CREATE_ACTIVITY, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, form.type.toString())
                    setTimestamp(3, Timestamp(System.currentTimeMillis()))
                    setString(4, id.toString())
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

    suspend fun getActivityById(id: String): Activity = withContext(Dispatchers.IO) {

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_ACTIVITY_BY_ID).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext Activity(
                            UUID.fromString(resultSet.getString("id")),
                            resultSet.getString("activityType")?.let {
                                try {
                                    ActivityType.valueOf(it.trim().uppercase())
                                } catch (e: IllegalArgumentException) {
                                    println("Invalid ActivityType value from DB: $it")
                                    null
                                }
                            },
                            resultSet.getTimestamp("creationDate"),
                            UUID.fromString(resultSet.getString("createdById"))
                        )
                    } else {
                        throw NoSuchElementException("No activity found with id $id")
                    }
                }
            }
        }
    }

    suspend fun getActivityByType(type: String): List<Activity> = withContext(Dispatchers.IO) {

        val activities = mutableListOf<Activity>()
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_ACTIVITY_BY_TYPE).use { statement ->
                statement.setString(1, type)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        activities.add(
                            Activity(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getString("activityType")?.let {
                                    try {
                                        ActivityType.valueOf(it.trim().uppercase())
                                    } catch (e: IllegalArgumentException) {
                                        println("Invalid ActivityType value from DB: $it")
                                        null
                                    }
                                },
                                resultSet.getTimestamp("creationDate"),
                                UUID.fromString(resultSet.getString("createdById"))
                            )
                        )
                    }
                    return@withContext activities
                }
            }
        }
    }

    suspend fun getActivityForUser(id: String): List<Activity> = withContext(Dispatchers.IO) {

        val activities = mutableListOf<Activity>()
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_ACTIVITY_BY_USER_ID).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        activities.add(
                            Activity(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getString("activityType")?.let {
                                    try {
                                        ActivityType.valueOf(it.trim().uppercase())
                                    } catch (e: IllegalArgumentException) {
                                        println("Invalid ActivityType value from DB: $it")
                                        null
                                    }
                                },
                                resultSet.getTimestamp("creationDate"),
                                UUID.fromString(resultSet.getString("createdById"))
                            )
                        )
                    }
                    return@withContext activities
                }
            }
        }
    }
}