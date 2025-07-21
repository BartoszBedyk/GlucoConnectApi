package infrastructure

import decryptField
import encryptField
import form.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import javax.crypto.SecretKey
import javax.sql.DataSource

class ResearchResultDao(private val dataSource: DataSource) {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                try {
                    statement.executeUpdate(SqlQueries.CREATE_GLUCOSE_TABLE)
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

    suspend fun createGlucoseResult(form: ResearchResultForm, secretKey: SecretKey): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()

        val (glucoseEncrypted, glucoseIv) = encryptField(form.glucoseConcentration.toString(), secretKey)
        val (afterMedEncrypted, afterMedIv) = encryptField(form.afterMedication.toString(), secretKey)
        val (emptyStomachEncrypted, emptyStomachIv) = encryptField(form.emptyStomach.toString(), secretKey)
        val (notesEncrypted, notesIv) = encryptField(form.notes ?: "", secretKey)


        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.CREATE_GLUCOSE_RESULT).use { statement ->
                statement.setString(1, id.toString())
                statement.setString(2, glucoseEncrypted)
                statement.setString(3, glucoseIv)
                statement.setString(4, form.unit)
                statement.setTimestamp(5, Timestamp(form.timestamp.time))
                statement.setString(6, afterMedEncrypted)
                statement.setString(7, afterMedIv)
                statement.setString(8, emptyStomachEncrypted)
                statement.setString(9, emptyStomachIv)
                statement.setString(10, notesEncrypted)
                statement.setString(11, notesIv)
                statement.setString(12, form.userId.toString())

                val rowsInserted = statement.executeUpdate()
                if (rowsInserted == 1) {
                    id
                } else {
                    throw IllegalStateException("Insert failed, no rows affected")
                }
            }
        }
    }

    suspend fun sync(result: GlucoseResult, secretKey: SecretKey): GlucoseResult = withContext(Dispatchers.IO) {
        // Sprawdź, czy rekord istnieje na serwerze

        val existsOnServer = dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.SYNC_GET).use { statement ->
                statement.setString(1, result.id.toString())
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                }
            }
        }

        // Szyfrujemy pola przed zapisem
        val (glucoseEncrypted, glucoseIv) = encryptField(result.glucoseConcentration.toString(), secretKey)
        val (afterMedEncrypted, afterMedIv) = encryptField(result.afterMedication.toString(), secretKey)
        val (emptyStomachEncrypted, emptyStomachIv) = encryptField(result.emptyStomach.toString(), secretKey)
        val (notesEncrypted, notesIv) = encryptField(result.notes ?: "", secretKey)

        if (existsOnServer) {

            dataSource.connection.use { connection ->
                connection.prepareStatement(SqlQueries.SYNC_UPDATE).use { statement ->
                    statement.apply {
                        setString(1, glucoseEncrypted)
                        setString(2, glucoseIv)
                        setString(3, result.unit)
                        setTimestamp(4, Timestamp(result.timestamp.time))
                        setString(5, afterMedEncrypted)
                        setString(6, afterMedIv)
                        setString(7, emptyStomachEncrypted)
                        setString(8, emptyStomachIv)
                        setString(9, notesEncrypted)
                        setString(10, notesIv)
                        setString(11, result.userId.toString())
                        setString(12, result.id.toString())
                    }
                    statement.executeUpdate()
                }
            }
        } else {
            dataSource.connection.use { connection ->
                connection.prepareStatement(SqlQueries.SYNC_INSERT).use { statement ->
                    statement.apply {
                        setString(1, result.id.toString())
                        setString(2, glucoseEncrypted)
                        setString(3, glucoseIv)
                        setString(4, result.unit)
                        setTimestamp(5, Timestamp(result.timestamp.time))
                        setString(6, afterMedEncrypted)
                        setString(7, afterMedIv)
                        setString(8, emptyStomachEncrypted)
                        setString(9, emptyStomachIv)
                        setString(10, notesEncrypted)
                        setString(11, notesIv)
                        setString(12, result.userId.toString())
                    }
                    statement.executeUpdate()
                }
            }
        }

        return@withContext result
    }


    suspend fun getGlucoseResultByIdBetweenDates(
        id: String, startDate: Date, endDate: Date, secretKey: SecretKey
    ): List<GlucoseResult> = withContext(Dispatchers.IO) {

        val results = mutableListOf<GlucoseResult>()
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_RESULTS_BY_USER_ID_BY_DATES).use { statement ->
                statement.setString(1, id)
                statement.setTimestamp(2, Timestamp(startDate.time))
                statement.setTimestamp(3, Timestamp(endDate.time))

                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val glucoseConcentration = decryptField(
                            resultSet.getString("glucose_concentration_encrypted"),
                            resultSet.getString("glucose_concentration_iv"),
                            secretKey
                        ).toDouble()

                        val afterMed = decryptField(
                            resultSet.getString("after_medication_encrypted"),
                            resultSet.getString("after_medication_iv"),
                            secretKey
                        ).toBoolean()

                        val emptyStomach = decryptField(
                            resultSet.getString("empty_stomach_encrypted"),
                            resultSet.getString("empty_stomach_iv"),
                            secretKey
                        ).toBoolean()

                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"), resultSet.getString("notes_iv"), secretKey
                        )

                        results.add(GlucoseResult(id = UUID.fromString(resultSet.getString("id")),
                            glucoseConcentration = glucoseConcentration,
                            unit = resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            timestamp = resultSet.getTimestamp("timestamp"),
                            userId = resultSet.getString("user_id")?.takeIf { it.isNotBlank() }
                                ?.let { UUID.fromString(it) },
                            deletedOn = Timestamp(System.currentTimeMillis()),  //remove it
                            lastUpdatedOn = resultSet.getTimestamp("last_updated_on"),
                            afterMedication = afterMed,
                            emptyStomach = emptyStomach,
                            notes = notes
                        )
                        )
                    }
                }
            }
        }
        results
    }


    suspend fun getGlucoseResultById(id: String, secretKey: SecretKey): GlucoseResult = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_RESULT_BY_ID).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val glucoseConc = decryptField(
                            resultSet.getString("glucose_concentration_encrypted"),
                            resultSet.getString("glucose_concentration_iv"),
                            secretKey
                        ).toDouble()

                        val afterMed = decryptField(
                            resultSet.getString("after_medication_encrypted"),
                            resultSet.getString("after_medication_iv"),
                            secretKey
                        ).toBoolean()

                        val emptyStomach = decryptField(
                            resultSet.getString("empty_stomach_encrypted"),
                            resultSet.getString("empty_stomach_iv"),
                            secretKey
                        ).toBoolean()

                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )

                        return@withContext GlucoseResult(
                            id = UUID.fromString(resultSet.getString("id")),
                            glucoseConcentration = glucoseConc,
                            unit = resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                            timestamp = resultSet.getTimestamp("timestamp"),
                            userId = resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                            deletedOn = Timestamp(System.currentTimeMillis()),  //remove it
                            lastUpdatedOn = resultSet.getTimestamp("last_updated_on"),
                            afterMedication = afterMed,
                            emptyStomach = emptyStomach,
                            notes = notes
                        )
                    } else {
                        throw NoSuchElementException("Record with ID $id not found")
                    }
                }
            }
        }
    }



    suspend fun getAll(secretKey: SecretKey): List<GlucoseResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GlucoseResult>()


        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_ALL_RESULTS).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val glucoseConc = decryptField(
                            resultSet.getString("glucose_concentration_encrypted"),
                            resultSet.getString("glucose_concentration_iv"),
                            secretKey
                        ).toDouble()

                        val afterMed = decryptField(
                            resultSet.getString("after_medication_encrypted"),
                            resultSet.getString("after_medication_iv"),
                            secretKey
                        ).toBoolean()

                        val emptyStomach = decryptField(
                            resultSet.getString("empty_stomach_encrypted"),
                            resultSet.getString("empty_stomach_iv"),
                            secretKey
                        ).toBoolean()

                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )

                        results.add(
                            GlucoseResult(
                                id = UUID.fromString(resultSet.getString("id")),
                                glucoseConcentration = glucoseConc,
                                unit = resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                                timestamp = resultSet.getTimestamp("timestamp"),
                                userId = resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                                deletedOn = Timestamp(System.currentTimeMillis()),
                                lastUpdatedOn = resultSet.getTimestamp("last_updated_on"),
                                afterMedication = afterMed,
                                emptyStomach = emptyStomach,
                                notes = notes
                            )
                        )
                    }
                }
            }
        }
        return@withContext results
    }


    suspend fun getThreeResultsForUser(id: String, secretKey: SecretKey): List<GlucoseResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GlucoseResult>()

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_THREE_RESULTS_FOR_USER).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val glucoseConc = decryptField(
                            resultSet.getString("glucose_concentration_encrypted"),
                            resultSet.getString("glucose_concentration_iv"),
                            secretKey
                        ).toDouble()

                        val afterMed = decryptField(
                            resultSet.getString("after_medication_encrypted"),
                            resultSet.getString("after_medication_iv"),
                            secretKey
                        ).toBoolean()

                        val emptyStomach = decryptField(
                            resultSet.getString("empty_stomach_encrypted"),
                            resultSet.getString("empty_stomach_iv"),
                            secretKey
                        ).toBoolean()

                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )

                        results.add(
                            GlucoseResult(
                                id = UUID.fromString(resultSet.getString("id")),
                                glucoseConcentration = glucoseConc,
                                unit = resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                                timestamp = resultSet.getTimestamp("timestamp"),
                                userId = resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                                deletedOn = Timestamp(System.currentTimeMillis()),
                                lastUpdatedOn = resultSet.getTimestamp("last_updated_on"),
                                afterMedication = afterMed,
                                emptyStomach = emptyStomach,
                                notes = notes
                            )
                        )
                    }
                }
            }
        }
        return@withContext results
    }

    suspend fun getResultsByUserId(id: String, secretKey: SecretKey): List<GlucoseResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<GlucoseResult>()

        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.GET_RESULTS_FOR_USER).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val glucoseConc = decryptField(
                            resultSet.getString("glucose_concentration_encrypted"),
                            resultSet.getString("glucose_concentration_iv"),
                            secretKey
                        ).toDouble()

                        val afterMed = decryptField(
                            resultSet.getString("after_medication_encrypted"),
                            resultSet.getString("after_medication_iv"),
                            secretKey
                        ).toBoolean()

                        val emptyStomach = decryptField(
                            resultSet.getString("empty_stomach_encrypted"),
                            resultSet.getString("empty_stomach_iv"),
                            secretKey
                        ).toBoolean()

                        val notes = decryptField(
                            resultSet.getString("notes_encrypted"),
                            resultSet.getString("notes_iv"),
                            secretKey
                        )

                        results.add(
                            GlucoseResult(
                                id = UUID.fromString(resultSet.getString("id")),
                                glucoseConcentration = glucoseConc,
                                unit = resultSet.getString("unit")?.let { PrefUnitType.valueOf(it) }.toString(),
                                timestamp = resultSet.getTimestamp("timestamp"),
                                userId = resultSet.getString("user_id")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
                                deletedOn = Timestamp(System.currentTimeMillis()),
                                lastUpdatedOn = resultSet.getTimestamp("last_updated_on"),
                                afterMedication = afterMed,
                                emptyStomach = emptyStomach,
                                notes = notes
                            )
                        )
                    }
                }
            }
        }
        return@withContext results
    }



    suspend fun updateResult(form: UpdateResearchResultForm, secretKey: SecretKey) = withContext(Dispatchers.IO) {

        val (glucoseEncrypted, glucoseIv) = encryptField(form.glucoseConcentration.toString(), secretKey)
        val (afterMedEncrypted, afterMedIv) = encryptField(form.afterMedication.toString(), secretKey)
        val (emptyStomachEncrypted, emptyStomachIv) = encryptField(form.emptyStomach.toString(), secretKey)
        val (notesEncrypted, notesIv) = encryptField(form.notes ?: "", secretKey)

        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(SqlQueries.UPDATE_RESULT).use { statement ->
                    statement.apply {
                        setString(1, glucoseEncrypted)
                        setString(2, glucoseIv)
                        setString(3, form.unit)
                        setTimestamp(4, form.timestamp?.let { Timestamp(it.time) })
                        setTimestamp(5, Timestamp(System.currentTimeMillis()))
                        setString(6, afterMedEncrypted)
                        setString(7, afterMedIv)
                        setString(8, emptyStomachEncrypted)
                        setString(9, emptyStomachIv)
                        setString(10, notesEncrypted)
                        setString(11, notesIv)
                        setString(12, form.id.toString())
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
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.HARD_DELETE).use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    suspend fun safeDeleteResult(form: SafeDeleteResultForm) = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(SqlQueries.SAFE_DELETE).use { statement ->
                statement.setBoolean(1, true)
                statement.setString(2, form.id.toString())
                statement.executeUpdate()
            }
        }
    }

}
