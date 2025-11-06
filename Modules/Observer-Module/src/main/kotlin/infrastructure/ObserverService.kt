package infrastructure

import form.CreateObserver
import form.Observer
import java.util.UUID


class ObserverService(private val observerDao: ObserverDao) {

    suspend fun observe(createObserver: CreateObserver) : UUID {
        return observerDao.observe(createObserver)
    }

    suspend fun getObservedAcceptedByObserverId(observerId: String): List<Observer>{
        return observerDao.getObservedAcceptedByObserverId(observerId)
    }

    suspend fun getObservedUnAcceptedByObserverId(observerId: String): List<Observer>{
        return observerDao.getObservedUnAcceptedByObserverId(observerId)
    }

    suspend fun getObservatorsByObservedIdUnAccepted(observedId: String): List<Observer>{
        return observerDao.getObservatorsByObservedIdUnAccepted(observedId)
    }
    suspend fun getObservatorsByObservedIdAccepted(observedId: String): List<Observer>{
        return observerDao.getObservatorsByObservedIdAccepted(observedId)
    }

    suspend fun acceptObservation(createObserver: CreateObserver): Int{
        return observerDao.acceptObservation(createObserver)
    }

    suspend fun unAcceptObservation(createObserver: CreateObserver): Int{
        return observerDao.unAcceptObservation(createObserver)
    }
}