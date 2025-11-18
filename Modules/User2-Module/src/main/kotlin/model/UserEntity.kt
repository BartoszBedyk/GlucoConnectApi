package model

import InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

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
