package model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UserEntity(
    val firstName: String,
    val lastName: String,
    val email: String,
    val type: UserType,
    val prefUnit: GlucoseUnit,
    @Contextual
    val createdAt: Instant?,
    @Contextual
    val updatedAt: Instant?
)
