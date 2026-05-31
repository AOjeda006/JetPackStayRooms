package com.example.jetpackstayrooms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jetpackstayrooms.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la tabla `rooms`.
 */
@Dao
interface RoomDao {

    /**
     * @return Identificador autogenerado (rowId) de la habitación insertada.
     */
    @Insert
    suspend fun insert(room: RoomEntity): Long

    @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: Long): RoomEntity?

    @Query("SELECT * FROM rooms")
    fun getAllRooms(): Flow<List<RoomEntity>>

    /** Devuelve las habitaciones con `isAvailable = true` (SQLite las codifica como `1`). */
    @Query("SELECT * FROM rooms WHERE isAvailable = 1")
    fun getAvailableRooms(): Flow<List<RoomEntity>>

    @Update
    suspend fun update(room: RoomEntity)

    /**
     * Actualización dirigida que modifica únicamente la columna `isAvailable`,
     * evitando reescribir el resto de campos.
     */
    @Query("UPDATE rooms SET isAvailable = :isAvailable WHERE id = :roomId")
    suspend fun updateRoomAvailability(roomId: Long, isAvailable: Boolean)
}