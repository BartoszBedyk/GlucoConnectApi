package domain

import data.ObserverRepository
import java.util.Observer
import java.util.UUID
import model.CreateObserverRequest

class ObserverService(private val observerRepository: ObserverRepository) {
    fun createObserver(request: CreateObserverRequest, userId: UUID): UUID = observerRepository.createObservation(request, userId)

    fun acceptObservation(observationId: UUID, userId: UUID) = observerRepository.acceptObservation(observationId, userId)

    fun unacceptObservation(observationId: UUID, userId: UUID) = observerRepository.unacceptObservation(observationId, userId)

    fun findObservationRequest(userId: UUID, accepted: Boolean) = observerRepository.getObservationRequests(userId, accepted)
}
