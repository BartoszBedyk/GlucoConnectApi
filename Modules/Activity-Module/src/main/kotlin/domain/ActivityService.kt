package domain

import data.ActivityRepository
import model.ActivityEntity
import model.CreateActivityRequest

class ActivityService(private val repository: ActivityRepository) {

    fun getAllActivities(): List<ActivityEntity> = repository.getAll()

    fun getActivityById(id: Int): ActivityEntity? = repository.findById(id)

    fun getActivityByUserId(userId: Int): List<ActivityEntity> = repository.findByUserId(userId)

    fun createActivity(request: CreateActivityRequest): Int = repository.create(request)
}
