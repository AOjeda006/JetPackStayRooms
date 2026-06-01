package com.aojeda.jetpackstayrooms.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Helper de cifrado de contraseñas basado en PBKDF2-HMAC-SHA256.
 *
 * Se elige PBKDF2 porque:
 * - Forma parte de `javax.crypto`, disponible en Android sin añadir
 *   dependencias externas (a diferencia de BCrypt o Argon2).
 * - Permite ajustar el coste computacional vía [ITERATIONS] sin cambiar el
 *   esquema de almacenamiento.
 *
 * El salt se genera con [SecureRandom] para cada usuario, lo que neutraliza
 * los ataques por rainbow table aun cuando varios usuarios elijan la misma
 * contraseña.
 *
 * Las cadenas devueltas/aceptadas están codificadas en Base64 sin saltos de
 * línea (`Base64.NO_WRAP`) para que sean seguras de almacenar en una columna
 * `TEXT` de SQLite.
 */
internal object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    /**
     * Genera un salt criptográficamente aleatorio listo para persistir.
     */
    fun newSalt(): String {
        val bytes = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Deriva el hash de [password] usando el [salt] dado y lo devuelve
     * codificado en Base64.
     */
    fun hash(password: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Verifica que [password] coincide con la pareja `(expectedHash, salt)`
     * previamente almacenada.
     *
     * La comparación es resistente a *timing attacks*: recorre todos los
     * bytes en tiempo constante independientemente de la posición de la
     * primera diferencia.
     */
    fun verify(password: String, expectedHash: String, salt: String): Boolean {
        val actual = hash(password, salt)
        return constantTimeEquals(actual, expectedHash)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}
