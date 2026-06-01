package com.aojeda.jetpackstayrooms.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aojeda.jetpackstayrooms.domain.User

/**
 * Fila persistible de la tabla `users`, contrapartida en la capa de datos de [User].
 *
 * Las credenciales se almacenan como `passwordHash` (PBKDF2-HMAC-SHA256) y un
 * `salt` aleatorio por usuario; ver
 * [com.aojeda.jetpackstayrooms.data.security.PasswordHasher] para el detalle
 * del algoritmo. Estos campos solo existen aquí: el mapeo a [User] los omite
 * para que el dominio nunca los conozca.
 *
 * El índice único sobre `username` lo aplica la base de datos, no la aplicación:
 * intentar insertar un usuario con un nombre ya existente lanza una excepción
 * SQLite de violación de restricción.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val name: String,
    val isOwner: Boolean = false
)

/**
 * Convierte la fila persistida en el modelo de dominio descartando
 * deliberadamente `passwordHash` y `salt`.
 */
fun UserEntity.toDomain() = User(
    id = id,
    username = username,
    name = name,
    isOwner = isOwner
)
