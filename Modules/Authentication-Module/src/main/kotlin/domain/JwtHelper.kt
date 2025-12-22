package domain

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.plugins.BadRequestException
import model.InnerUserEntity
import java.util.Date

open class JwtHelper {

    private val dotenv = dotenv()

    private val secretKey = dotenv["SECRET_KEY"]
    private val audience = dotenv["AUDIENCE"]
    private val issuer = dotenv["ISSUER"]

    private val tokenFreshness: Long = 7L * 24 * 60 * 60 * 1000

    private val nullTokenMessage = "The authentication token is null."
    private val nullDateInTokenMessage = "The authentication token doesnt contain a date."
    private val tooOldTokenMessage = "The authentication token is obsolete."

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
        if (token.equals(null)) throw BadRequestException(nullTokenMessage)
    }

    private fun createVerifier(): JWTVerifier = JWT.require(Algorithm.HMAC256(secretKey))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    private fun validateTokenFreshness(tokenExpiration: Date) {
        val now = Date()

        if (tokenExpiration.equals(null)) {
            throw BadRequestException(nullDateInTokenMessage)
        }

        val timeToExpiration = tokenExpiration.time - now.time

        if (timeToExpiration > tokenFreshness) {
            throw BadRequestException(tooOldTokenMessage)
        }
    }
}
