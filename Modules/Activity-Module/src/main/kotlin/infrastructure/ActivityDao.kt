package infrastructure

import form.Activity
import form.ActivityType
import form.CreteActivityForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

class ActivityDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """CREATE TABLE IF NOT EXISTS activities (
        id CHAR(36) NOT NULL PRIMARY KEY,
        activityType CHAR(50) NOT NULL,
        creationDate TIMESTAMP,
        createdById CHAR(36) NOT NULL);
           """
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                try{
                    statement.executeUpdate(createTableQuery)
                } catch (e: SQLException) {
                    if (!e.message?.contains("already exists")!!) {
                        throw e
                    } else {
                        // tak nie sprawdzę tego nigdy
                    }
                }
            }
        }
    }

    suspend fun create(form: CreteActivityForm): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val insertQuery = """INSERT INTO activities (id, activityType, creationDate, createdById)
            VALUES (?, ?, ?, ?)
        """
        dataSource.connection.use { connection ->
            connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, form.type.toString())
                    setTimestamp(3, Timestamp(System.currentTimeMillis()))
                    setString(4,id.toString())
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
        val selectQuery = "SELECT id, activityType, creationDate, createdById FROM activities WHERE id =?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if(resultSet.next()){
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

    suspend fun getActivityByType(type : String): List<Activity> = withContext(Dispatchers.IO)  {
        val selectQuery = "SELECT id, activityType, creationDate, createdById FROM activities WHERE activityType = ?"
        val activities = mutableListOf<Activity>()
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, type)
                statement.executeQuery().use { resultSet ->
                    while(resultSet.next()){
                        activities.add(Activity(
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
                        ))
                    }
                    return@withContext activities
                }
            }
        }
    }

    suspend fun getActivityForUser(id : String): List<Activity> = withContext(Dispatchers.IO)  {
        val selectQuery = "SELECT id, activityType, creationDate, createdById FROM activities WHERE createdbyid = ?"
        val activities = mutableListOf<Activity>()
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while(resultSet.next()){
                        activities.add(Activity(
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
                        ))
                    }
                    return@withContext activities
                }
            }
        }
    }
}