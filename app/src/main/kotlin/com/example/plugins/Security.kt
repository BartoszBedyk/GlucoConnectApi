package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
val secretKey = "ff330088dd22aa562273d0b24fb04791ce7237129da2fbb44fb12a78d420788c".hexStringToByteArray()

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
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ktor.io"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secretKey))
                    .withAudience("myaudience")
                    .withIssuer("myissuer")
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains("myaudience")) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
