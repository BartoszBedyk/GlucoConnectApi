package infrastructure

import form.Activity
import form.CreteActivityForm
import java.util.*

class ActivityService(private val activityDao: ActivityDao) {

    suspend fun createActivity(form: CreteActivityForm) : UUID {
        return activityDao.create(form)
    }

    suspend fun getActivityById(id: String) : Activity {
        return activityDao.getActivityById(id)
    }
}