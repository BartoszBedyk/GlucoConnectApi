package form

import kotlinx.serialization.Serializable

@Serializable
data class CreateObserver(val observerId: String, val observedId: String)
