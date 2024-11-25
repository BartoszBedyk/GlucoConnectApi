package infrastructure

import form.CreateMedication
import form.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import javax.sql.DataSource

class MedicationsDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """CREATE TABLE IF NOT EXISTS public.medications (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    manufacturer VARCHAR(100),
    form VARCHAR(50), 
    strength VARCHAR(50)
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

    suspend fun createMedication(createMedicationForm: CreateMedication): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        val createUserQuery = """INSERT INTO public.medications (id, name, description, manufacturer, form, strength) 
VALUES 
(?,?,?,?,?,?);
 """
        dataSource.connection.use { connection ->
            connection.prepareStatement(createUserQuery, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.apply {
                    setString(1, id.toString())
                    setString(2, createMedicationForm.name)
                    setString(3, createMedicationForm.description)
                    setString(4, createMedicationForm.manufacturer)
                    setString(5, createMedicationForm.form)
                    setString(6, createMedicationForm.strength)
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


    suspend fun readMedication(id: String): Medication = withContext(Dispatchers.IO) {
        val readUserQuery = """SELECT id, name, description, manufacturer, form, strength
FROM public.medications
WHERE id = ?;
"""

        dataSource.connection.use { connection ->
            connection.prepareStatement(readUserQuery).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext Medication(
                            UUID.fromString(resultSet.getString("id")),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getString("manufacturer"),
                            resultSet.getString("form"),
                            resultSet.getString("strength")
                        )
                    } else {
                        throw NoSuchElementException("Record with ID $id not found")
                    }
                }
            }
        }
    }

    suspend fun getAll() = withContext(Dispatchers.IO) {
        val medications = mutableListOf<Medication>()
        val selectAllQuery = "SELECT * FROM public.medications"
        dataSource.connection.use { connection ->
            connection.prepareStatement(selectAllQuery).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        medications.add(
                            Medication(
                                UUID.fromString(resultSet.getString("id")),
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                resultSet.getString("manufacturer"),
                                resultSet.getString("form"),
                                resultSet.getString("strength")
                            )
                        )
                    }
                }
            }
            return@withContext medications
        }

    }

    suspend fun deleteMedication(id: String) = withContext(Dispatchers.IO) {
        val deleteQuery = "DELETE FROM public.medications WHERE id = ?"
        dataSource.connection.use { connection ->
            connection.prepareStatement(deleteQuery).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }


}