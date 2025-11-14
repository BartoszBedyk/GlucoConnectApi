package model

import InstantSerializer
import java.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserEntity(
    val firstName: String,
    val lastName: String,
    val email: String,
    val type: UserType,
    val prefUnit: GlucoseUnit,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant?,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant?
)
