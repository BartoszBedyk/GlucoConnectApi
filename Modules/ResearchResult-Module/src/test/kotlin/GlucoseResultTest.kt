import form.PrefUnitType
import form.GlucoseResult
import infrastructure.ResearchResultDao
import infrastructure.ResearchResultService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.sql.Timestamp
import java.util.*
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlucoseResultTest {

    private val mockDao = mock<ResearchResultDao>()
    private val service = ResearchResultService(mockDao)

    @Test
    fun `should return ResearchResult object if exists`() = runBlocking {
        val result = GlucoseResult(
            id = UUID.fromString("b66382f2-e5dc-48f1-aa9a-823e49c498b0"),
            glucoseConcentration = 100.0,
            unit = PrefUnitType.MG_PER_DL.toString() ,
            timestamp = Timestamp(System.currentTimeMillis()),
            userId = UUID.fromString( "0e464cdf-859d-4daa-bbc3-b1a580c60249"),
            deletedOn = null ,
            lastUpdatedOn = null ,
            afterMedication = true,
            emptyStomach = false ,
            notes = "Notes are available"
        )

        whenever(mockDao.getGlucoseResultById("b66382f2-e5dc-48f1-aa9a-823e49c498b0")).thenReturn(result)
        val reaserch = service.getGlucoseResultById("b66382f2-e5dc-48f1-aa9a-823e49c498b0")

        val expected = Timestamp(System.currentTimeMillis())
        val actual = reaserch.timestamp

        val timeDiff = abs(expected.time - actual.time)
        assertTrue(timeDiff < 1000, "Timestamps differ by more than 1 second")

        assertEquals(result.id, reaserch.id)
        assertEquals(100.0, reaserch.glucoseConcentration)
        assertEquals(PrefUnitType.MG_PER_DL.toString(), reaserch.unit)
        assertEquals(UUID.fromString( "0e464cdf-859d-4daa-bbc3-b1a580c60249"), reaserch.userId)
        assertEquals(null, reaserch.deletedOn)
        assertEquals(null, reaserch.lastUpdatedOn)
        assertEquals(true, reaserch.afterMedication)
        assertEquals(false, reaserch.emptyStomach)
        assertEquals("Notes are available", reaserch.notes)
    }

}