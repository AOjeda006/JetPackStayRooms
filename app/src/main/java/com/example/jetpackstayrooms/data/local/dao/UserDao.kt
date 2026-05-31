package com.example.jetpackstayrooms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jetpackstayrooms.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la tabla `users`. Las funciones `suspend` se ejecutan fuera del hilo
 * principal; las que devuelven [Flow] se observan continuamente y emiten en
 * cada cambio de la tabla.
 */
@Dao
interface UserDao {

    /**
     * Inserta un nuevo usuario.
     *
     * @return Identificador autogenerado (rowId) del usuario insertado.
     * @throws android.database.sqlite.SQLiteConstraintException si el `username`
     *  ya existe (violación del índice único).
     */
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
}