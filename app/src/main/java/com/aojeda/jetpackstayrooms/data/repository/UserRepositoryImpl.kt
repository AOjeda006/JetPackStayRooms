package com.aojeda.jetpackstayrooms.data.repository

import com.aojeda.jetpackstayrooms.data.local.dao.UserDao
import com.aojeda.jetpackstayrooms.data.local.entity.UserEntity
import com.aojeda.jetpackstayrooms.data.local.entity.toDomain
import com.aojeda.jetpackstayrooms.data.security.PasswordHasher
import com.aojeda.jetpackstayrooms.domain.User
import com.aojeda.jetpackstayrooms.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de [UserRepository] respaldada por Room.
 *
 * Es el único punto del sistema que ve contraseñas en claro: las recibe en
 * [insertUser] / [login], las pasa por [PasswordHasher] y solo persiste el
 * hash y el salt. El dominio nunca conoce las credenciales.
 */
class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun insertUser(user: User, password: String): Long {
        val salt = PasswordHasher.newSalt()
        val passwordHash = PasswordHasher.hash(password, salt)
        val entity = UserEntity(
            id = user.id,
            username = user.username,
            passwordHash = passwordHash,
            salt = salt,
            name = user.name,
            isOwner = user.isOwner
        )
        return userDao.insert(entity)
    }

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)?.toDomain()
    }

    override suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)?.toDomain()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun login(username: String, password: String): User? {
        val entity = userDao.getUserByUsername(username) ?: return null
        return if (PasswordHasher.verify(password, entity.passwordHash, entity.salt)) {
            entity.toDomain()
        } else {
            null
        }
    }
}
