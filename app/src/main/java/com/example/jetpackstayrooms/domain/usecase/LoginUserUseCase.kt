package com.example.jetpackstayrooms.domain.usecase

import com.example.jetpackstayrooms.domain.User
import com.example.jetpackstayrooms.domain.repository.UserRepository

/**
 * Autentica a un usuario contra el repositorio comprobando credenciales.
 *
 * Diferencia explícitamente "campos vacíos" de "credenciales incorrectas" para
 * que la UI pueda mostrar el mensaje adecuado.
 */
class LoginUserUseCase(private val userRepository: UserRepository) {

    /**
     * @return [Result.success] con el [User] autenticado; [Result.failure] si los
     *  campos vienen vacíos, si las credenciales no coinciden con ningún usuario
     *  o si ocurre un error al consultar la base de datos.
     */
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return try {
            if (username.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Usuario y contraseña son obligatorios"))
            }

            val user = userRepository.login(username, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}