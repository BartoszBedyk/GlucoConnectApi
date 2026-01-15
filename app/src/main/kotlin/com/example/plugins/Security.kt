package com.example.plugins

import UserPrincipal
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.util.UUID

@Suppress("MagicNumber")
fun String.hexStringToByteArray(): ByteArray {
    val len = this.length
    require(len % 2 == 0) { "Hex string must have an even length" }

    val result = ByteArray(len / 2)
    for (i in 0 until len step 2) {
        val high = this[i].digitToIntOrNull(16) ?: error("Invalid hex character: ${this[i]}")
        val low = this[i + 1].digitToIntOrNull(16) ?: error("Invalid hex character: ${this[i + 1]}")
        result[i / 2] = ((high shl 4) + low).toByte()
    }
    return result
}

fun Application.configureSecurity() {
    val dotenv = dotenv()
    val secretKey = dotenv["SECRET_KEY"]
    val audience = dotenv["AUDIENCE"]
    val issuer = dotenv["ISSUER"]

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ktor sample app"

            verifier(
                JWT
                    .require(Algorithm.HMAC256(secretKey))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload
                    .getClaim("userId")
                    .asString()

                val userType = credential.payload
                    .getClaim("userType")
                    .asString()

                if (userId != null && userType != null) {
                    UserPrincipal(
                        id = UUID.fromString(userId),
                        userType = userType
                    )
                } else null
            }

            challenge { _, _ ->
                call.respond(UnauthorizedResponse())
            }
        }
    }
}

