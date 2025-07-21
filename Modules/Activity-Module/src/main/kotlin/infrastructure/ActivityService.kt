package infrastructure

import form.Activity
import form.CreteActivityForm
import java.util.*

class ActivityService(private val activityDao: ActivityDao) {

    suspend fun createActivity(form: CreteActivityForm) : UUID {
        return activityDao.createActivity(form)
    }

    suspend fun getActivityById(id: String) : Activity {
        return activityDao.getActivityById(id)
    }

    suspend fun getActivityForUser(id: String) : List<Activity> {
        return activityDao.getActivityForUser(id)
    }

    suspend fun getActivityByType(type: String) : List<Activity> {
        return activityDao.getActivityByType(type)
    }
}