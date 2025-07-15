import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.util.*

fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
    return BCrypt.checkpw(plainPassword, hashedPassword)
}

fun hashEmail(email: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val normalizedEmail = email.trim().lowercase()
    val hashBytes = digest.digest(normalizedEmail.toByteArray(Charsets.UTF_8))
    return Base64.getEncoder().encodeToString(hashBytes)
}