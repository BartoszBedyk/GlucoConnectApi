package domain

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import model.InnerUserEntity
import java.util.Date

open class JwtHelper {

    private val dotenv = dotenv()

    private val secretKey = dotenv["SECRET_KEY"]
    private val audience = dotenv["AUDIENCE"]
    private val issuer = dotenv["ISSUER"]

    private val tokenFreshness: Long = 7L * 24 * 60 * 60 * 1000

    fun createToken(user: InnerUserEntity): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", user.id)
        .withClaim("userType", user.type)
        .withClaim("email", user.email)
        .withExpiresAt(Date(System.currentTimeMillis() + tokenFreshness))
        .sign(Algorithm.HMAC256(secretKey))

    fun refreshToken(currentToken: String): String {
        isTokenNull(currentToken)
        val verifier = createVerifier()
        val decoded = verifier.verify(currentToken)

        validateTokenFreshness(decoded.expiresAt)

        return createToken(
            InnerUserEntity(
                decoded.claims["userId"].toString(),
                decoded.claims["userType"].toString(),
                decoded.claims["email"].toString()
            )
        )
    }

    private fun isTokenNull(token: String) {
        if (token.equals(null)) throw IllegalArgumentException()
    }

    private fun createVerifier(): JWTVerifier = JWT.require(Algorithm.HMAC256(secretKey))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    private fun validateTokenFreshness(tokenExpiration: Date) {
        val now = Date()

        if (tokenExpiration.equals(null) || tokenExpiration.after(now)) {
            throw IllegalArgumentException()
        }

        val timeToExpiration = tokenExpiration.time - now.time

        if (timeToExpiration > tokenFreshness) {
            throw IllegalArgumentException()
        }
    }
}
