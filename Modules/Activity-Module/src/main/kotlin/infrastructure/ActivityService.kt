package infrastructure

import form.Activity
import form.CreteActivityForm
import java.util.UUID

class ActivityService(private val activityDao: ActivityDao) {

    suspend fun createActivity(form: CreteActivityForm): UUID = activityDao.createActivity(form)

    suspend fun getActivityById(id: String): Activity = activityDao.getActivityById(id)

    suspend fun getActivityForUser(id: String): List<Activity> = activityDao.getActivityForUser(id)

    suspend fun getActivityByType(type: String): List<Activity> = activityDao.getActivityByType(type)
}
