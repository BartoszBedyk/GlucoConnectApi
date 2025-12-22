

import io.ktor.server.auth.Principal
import java.util.UUID

 data class UserPrincipal(
    val id: UUID,
    val userType: String
    ) : Principal
