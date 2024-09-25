package form

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType {
    CREATE_DOCTOR,
    CREATE_ADMIN,
    CREATE_PATIENT,
    CREATE_OBSERVER,
    CREATE_EMPTY_USER,
    ADD_RESULT,
    ADD_RESULTS,
    DELETE_RESULT,
    DELETE_USER_ANY,
    UPDATE_USER,
    UPDATE_RESULT,
    READ_USER,
    READ_RESULT,
    READ_USERS,
    READ_RESULTS,


}