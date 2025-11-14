package domain

import data.ActivityRepository
import model.ActivityEntity
import model.CreateActivityRequest
import pageable.PageRequest
import pageable.PageResponse

class ActivityService(private val repository: ActivityRepository) {

    fun getAllActivities(pageRequest: PageRequest): PageResponse<ActivityEntity> = repository.findAll(pageRequest)

    fun getActivityById(id: Int): ActivityEntity? = repository.findById(id)

    fun getActivityByUserId(userId: Int, pageRequest: PageRequest): PageResponse<ActivityEntity> =
        repository.findByUserId(userId, pageRequest)

    fun createActivity(request: CreateActivityRequest): Int = repository.create(request)
}
