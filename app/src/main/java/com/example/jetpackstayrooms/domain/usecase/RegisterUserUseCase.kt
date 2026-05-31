package com.example.jetpackstayrooms.domain.usecase

import com.example.jetpackstayrooms.domain.User
import com.example.jetpackstayrooms.domain.repository.UserRepository

/**
 * Registra un nuevo usuario tipo cliente (no propietario).
 *
 * Aplica las reglas de validación previas a la persistencia: unicidad del
 * `username`, campos no vacíos y longitud mínima de la contraseña.
 *
 * Los errores no se propagan como excepciones: se devuelven envueltos en
 * [Result.failure] para que la capa de presentación los muestre como mensaje
 * de UI.
 */
class RegisterUserUseCase(private val userRepository: UserRepository) {

    /**
     * Crea el usuario tras validar los datos.
     *
     * @return [Result.success] con el [User] persistido (incluye `id` asignado);
     *  [Result.failure] si el usuario ya existe, si hay campos vacíos, si la
     *  contraseña tiene menos de 6 caracteres o si ocurre un error de persistencia.
     */
    suspend operator fun invoke(username: String, password: String, name: String): Result<User> {
        return try {
            // 1. Validaciones de formato (baratas, sin tocar BD)
            if (username.isBlank() || password.isBlank() || name.isBlank()) {
                return Result.failure(Exception("Todos los campos son obligatorios"))
            }

            if (password.length < 6) {
                return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
            }

            // 2. Validación de unicidad (consulta a BD)
            val existingUser = userRepository.getUserByUsername(username)
            if (existingUser != null) {
                return Result.failure(Exception("El usuario ya existe"))
            }

            // 3. Persistir
            val user = User(
                username = username,
                name = name,
                isOwner = false
            )

            val userId = userRepository.insertUser(user, password)
            Result.success(user.copy(id = userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}