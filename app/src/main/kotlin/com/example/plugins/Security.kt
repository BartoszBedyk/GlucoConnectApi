package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*


fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ktor.io"
            verifier(
                JWT
                    .require(Algorithm.HMAC256("secret"))
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
