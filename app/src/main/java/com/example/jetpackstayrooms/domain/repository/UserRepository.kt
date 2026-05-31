package com.example.jetpackstayrooms.domain.repository

import com.example.jetpackstayrooms.domain.User
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de acceso a usuarios definido por el dominio.
 *
 * La capa de datos provee la implementación concreta. Los casos de uso dependen
 * únicamente de esta interfaz, no del DAO ni de Room, lo que permite sustituir el
 * origen de datos sin afectar al dominio.
 */
interface UserRepository {

    /**
     * Persiste un nuevo usuario aplicando el cifrado de la contraseña.
     *
     * El [password] se recibe aparte de [User] porque no forma parte del
     * modelo de dominio: la implementación lo hashea con
     * [com.example.jetpackstayrooms.data.security.PasswordHasher] antes de
     * guardarlo, de modo que la contraseña en claro no sobrevive a esta
     * llamada.
     *
     * @return Identificador autogenerado del usuario insertado.
     */
    suspend fun insertUser(user: User, password: String): Long

    /**
     * Busca un usuario por su nombre de inicio de sesión.
     *
     * @return El usuario encontrado, o `null` si no existe ninguno con ese nombre.
     */
    suspend fun getUserByUsername(username: String): User?

    /**
     * @return El usuario con el [userId] dado, o `null` si no existe.
     */
    suspend fun getUserById(userId: Long): User?

    /**
     * Flujo reactivo con todos los usuarios registrados; emite en cada cambio.
     */
    fun getAllUsers(): Flow<List<User>>

    /**
     * Comprueba credenciales y devuelve el usuario coincidente.
     *
     * @return El usuario si las credenciales son correctas, o `null` si no hay
     *  coincidencia.
     */
    suspend fun login(username: String, password: String): User?
}