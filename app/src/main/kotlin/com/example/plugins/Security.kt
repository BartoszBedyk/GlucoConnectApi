package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.server.response.*

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
                println("JWT payload audience: ${credential.payload.audience}")
                if (credential.payload.audience.contains(audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(UnauthorizedResponse())
            }
        }
    }
}

