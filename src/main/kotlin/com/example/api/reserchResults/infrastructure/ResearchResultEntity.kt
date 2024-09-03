package com.example.api.researchResults.infrastructure

import UUIDSerializer

import com.example.api.reserchResults.domain.form.ResearchResult
import com.example.api.reserchResults.domain.form.ResearchResultForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.*
import java.util.*

@Serializable
class ResearchResultEntity(private val connection: Connection) {

    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID()

    companion object {
        private const val CREATE_TABLE_RESEARCH_RESULTS = """
    CREATE TABLE IF NOT EXISTS public.glucosemeasurements (
        ID CHAR(36) PRIMARY KEY,
        sequenceNumber INT NOT NULL,
        glucoseConcentration DOUBLE PRECISION NOT NULL,
        unit VARCHAR(30) NOT NULL,
        timestamp TIMESTAMP NOT NULL
    );
"""
        private const val INSERT_RESEARCH_RESULTS = """
    INSERT INTO public.glucosemeasurements (ID, sequenceNumber, glucoseConcentration, unit, timestamp)
    VALUES (?, ?, ?, ?, ?);
"""
        private const val SELECT_RESULT_BY_ID = """
    SELECT ID, sequenceNumber, glucoseConcentration, unit, timestamp
    FROM public.glucosemeasurements
    WHERE ID = ?
"""
    }

    init {
        val statement = connection.createStatement()
        try {
            statement.executeUpdate(CREATE_TABLE_RESEARCH_RESULTS)
        } catch (e: SQLException) {
            if (!e.message?.contains("already exists")!! == true) {
                throw e
            }
        }
    }

    suspend fun create(form: ResearchResultForm): UUID = withContext(Dispatchers.IO) {
        val id: UUID = UUID.randomUUID()
        connection.prepareStatement(INSERT_RESEARCH_RESULTS, Statement.RETURN_GENERATED_KEYS).use { statement ->
            statement.apply {
                setString(1, id.toString())
                setInt(2, form.sequenceNumber)
                setDouble(3, form.glucoseConcentration)
                setString(4, form.unit)
                setTimestamp(5, Timestamp(form.timestamp.time))
            }
            statement.executeUpdate()
            statement.generatedKeys.use { generatedKeys ->
                if (generatedKeys.next()) {
                    return@withContext UUID.fromString(generatedKeys.getString(1))
                } else {
                    throw IllegalStateException("Generated keys not found")
                }
            }
        }
    }

    suspend fun read(id: String): ResearchResult = withContext(Dispatchers.IO) {
        connection.prepareStatement(SELECT_RESULT_BY_ID).use { statement ->
            statement.setString(1, id)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    val ID = UUID.fromString(resultSet.getString("ID"))
                    val sequenceNumber = resultSet.getInt("sequenceNumber")
                    val glucoseConcentration = resultSet.getDouble("glucoseConcentration")
                    val unit = resultSet.getString("unit")
                    val timestamp = resultSet.getTimestamp("timestamp")
                    return@withContext ResearchResult(ID, sequenceNumber, glucoseConcentration, unit, timestamp)
                } else {
                    throw NoSuchElementException("Record with ID $id not found")
                }
            }
        }
    }
}
