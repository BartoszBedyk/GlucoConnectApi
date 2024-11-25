package infrastructure

import form.HeartbeatForm
import form.HeartbeatReturn
import java.util.*

class HeartbeatResultService(private val resultDao: HeartbeatResultDao) {

    suspend fun createResult(form: HeartbeatForm): UUID {
        return resultDao.createHeartbeatResult(form)
    }

    suspend fun readResultById(id: String): HeartbeatReturn {
        return resultDao.readById(id)
    }

    suspend fun readResultByUserId(userId: String): List<HeartbeatReturn> {
        return resultDao.getHeartbeatByUserId(userId)
    }

    suspend fun deleteHeartbeatResult(id: String): Int {
        return resultDao.deleteResult(id)
    }

    suspend fun deleteHeartbeatResultByUser(id: String): Int {
        return resultDao.deleteResultsForUser(id)
    }
}