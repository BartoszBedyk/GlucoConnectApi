package infrastructure

import form.CreateObserver
import form.Observer
import java.util.UUID

class ObserverService(private val observerDao: ObserverDao) {

    suspend fun observe(createObserver: CreateObserver): UUID = observerDao.observe(createObserver)

    suspend fun getObservedAcceptedByObserverId(observerId: String): List<Observer> =
        observerDao.getObservedAcceptedByObserverId(observerId)

    suspend fun getObservedUnAcceptedByObserverId(observerId: String): List<Observer> =
        observerDao.getObservedUnAcceptedByObserverId(observerId)

    suspend fun getObservatorsByObservedIdUnAccepted(observedId: String): List<Observer> =
        observerDao.getObservatorsByObservedIdUnAccepted(observedId)
    suspend fun getObservatorsByObservedIdAccepted(observedId: String): List<Observer> =
        observerDao.getObservatorsByObservedIdAccepted(observedId)

    suspend fun acceptObservation(createObserver: CreateObserver): Int = observerDao.acceptObservation(createObserver)

    suspend fun unAcceptObservation(createObserver: CreateObserver): Int =
        observerDao.unAcceptObservation(createObserver)
}
