package com.aojeda.jetpackstayrooms.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests de [PasswordHasher].
 *
 * Se ejecutan con Robolectric porque [PasswordHasher] usa `android.util.Base64`,
 * que en un test JVM puro está stubbeado y lanzaría "Method not mocked".
 * Robolectric aporta la implementación real del framework.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PasswordHasherTest {

    @Test
    fun `hash no devuelve la contraseña en claro y es determinista para el mismo salt`() {
        val password = "S3gura!2024"
        val salt = PasswordHasher.newSalt()

        val hash = PasswordHasher.hash(password, salt)

        // El hash no debe coincidir con la contraseña original.
        assertNotEquals(password, hash)
        // Con el mismo salt y contraseña el hash es reproducible.
        assertEquals(hash, PasswordHasher.hash(password, salt))
    }

    @Test
    fun `verify acepta la contraseña correcta y rechaza la incorrecta`() {
        val salt = PasswordHasher.newSalt()
        val expectedHash = PasswordHasher.hash("correcta", salt)

        assertTrue(PasswordHasher.verify("correcta", expectedHash, salt))
        assertFalse(PasswordHasher.verify("incorrecta", expectedHash, salt))
    }

    @Test
    fun `salts distintos producen hashes distintos para la misma contraseña`() {
        val password = "misma-contraseña"

        val hashA = PasswordHasher.hash(password, PasswordHasher.newSalt())
        val hashB = PasswordHasher.hash(password, PasswordHasher.newSalt())

        assertNotEquals(hashA, hashB)
    }
}
