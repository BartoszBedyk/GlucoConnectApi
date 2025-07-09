package infrastructure

import form.HeartbeatForm
import form.HeartbeatReturn
import java.util.*
import javax.crypto.SecretKey

class HeartbeatResultService(private val resultDao: HeartbeatResultDao, private val secretKey: SecretKey) {

    suspend fun createResult(form: HeartbeatForm): UUID {
        return resultDao.createHeartbeatResult(form, secretKey)
    }

    suspend fun readResultById(id: String): HeartbeatReturn {
        return resultDao.readById(id, secretKey)
    }

    suspend fun readResultByUserId(userId: String): List<HeartbeatReturn> {
        return resultDao.getHeartbeatByUserId(userId, secretKey)
    }

    suspend fun getThreeHeartbeatResults(userId: String): List<HeartbeatReturn> {
        return resultDao.getThreeHeartbeatResults(userId, secretKey)
    }

    suspend fun deleteHeartbeatResult(id: String): Int {
        return resultDao.deleteResult(id)
    }

    suspend fun deleteHeartbeatResultByUser(id: String): Int {
        return resultDao.deleteResultsForUser(id)
    }
}