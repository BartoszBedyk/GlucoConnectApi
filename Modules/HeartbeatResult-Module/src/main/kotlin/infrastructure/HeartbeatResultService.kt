package infrastructure

import form.HeartbeatForm
import form.HeartbeatReturn
import java.util.UUID
import javax.crypto.SecretKey

class HeartbeatResultService(private val resultDao: HeartbeatResultDao, private val secretKey: SecretKey) {

    suspend fun createResult(form: HeartbeatForm): UUID = resultDao.createHeartbeatResult(form, secretKey)

    suspend fun readResultById(id: String): HeartbeatReturn = resultDao.getHeartbeatById(id, secretKey)

    suspend fun readResultByUserId(userId: String): List<HeartbeatReturn> =
        resultDao.getHeartbeatByUserId(userId, secretKey)

    suspend fun getThreeHeartbeatResults(userId: String): List<HeartbeatReturn> =
        resultDao.getThreeHeartbeatResults(userId, secretKey)

    suspend fun deleteHeartbeatResult(id: String): Int = resultDao.deleteResult(id)

    suspend fun deleteHeartbeatResultByUser(id: String): Int = resultDao.deleteResultsForUser(id)
}
