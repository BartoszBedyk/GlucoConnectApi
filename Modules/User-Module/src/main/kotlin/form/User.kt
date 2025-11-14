package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val password: String,
    val type: UserType?,
    val isBlocked: Boolean?,
    val prefUnit: String?,
    val diabetesType: DiabetesType?
)

enum class UserType {
    ADMIN,
    PATIENT,
    DOCTOR,
    OBSERVER
}

enum class PrefUnitType {
    MG_PER_DL,
    MMOL_PER_L
}

enum class DiabetesType(val description: String) {
    TYPE_1("typu pierwszego"),
    TYPE_2("typu drugiego"),
    MODY("MODY"),
    LADA("LADA"),
    GESTATIONAL("ciążowa"),
    NONE("brak")
}
