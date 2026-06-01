package com.aojeda.jetpackstayrooms.data.repository

import com.aojeda.jetpackstayrooms.data.local.dao.RoomDao
import com.aojeda.jetpackstayrooms.data.local.entity.toDomain
import com.aojeda.jetpackstayrooms.data.local.entity.toEntity
import com.aojeda.jetpackstayrooms.domain.Room
import com.aojeda.jetpackstayrooms.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de [RoomRepository] respaldada por Room.
 *
 * Adapta el flujo de [RoomDao] al modelo de dominio mapeando cada
 * [com.aojeda.jetpackstayrooms.data.local.entity.RoomEntity] a [Room].
 */
class RoomRepositoryImpl(private val roomDao: RoomDao) : RoomRepository {
    override suspend fun insertRoom(room: Room): Long {
        return roomDao.insert(room.toEntity())
    }

    override suspend fun getRoomById(roomId: Long): Room? {
        return roomDao.getRoomById(roomId)?.toDomain()
    }

    override fun getAllRooms(): Flow<List<Room>> {
        return roomDao.getAllRooms().map { list -> list.map { it.toDomain() } }
    }

    override fun getAvailableRooms(): Flow<List<Room>> {
        return roomDao.getAvailableRooms().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateRoom(room: Room) {
        roomDao.update(room.toEntity())
    }

    override suspend fun updateRoomAvailability(roomId: Long, isAvailable: Boolean) {
        roomDao.updateRoomAvailability(roomId, isAvailable)
    }
}