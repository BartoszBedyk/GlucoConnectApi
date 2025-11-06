import java.security.SecureRandom
import java.util.Base64


import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


fun encryptField(plainText: String, secretKey: SecretKey): Pair<String, String> {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
    val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes)
    val ivBase64 = Base64.getEncoder().encodeToString(iv)
    return encryptedBase64 to ivBase64
}

fun decryptField(encryptedBase64: String, ivBase64: String, secretKey: SecretKey): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val iv = Base64.getDecoder().decode(ivBase64)
    val encryptedBytes = Base64.getDecoder().decode(encryptedBase64)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes, Charsets.UTF_8)
}

fun loadSecretKey(base64EncodedKey: String): SecretKey {
    val decodedKey = Base64.getDecoder().decode(base64EncodedKey)
    return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
}

