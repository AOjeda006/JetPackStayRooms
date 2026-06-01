package com.aojeda.jetpackstayrooms.domain.usecase

import com.aojeda.jetpackstayrooms.domain.Room
import com.aojeda.jetpackstayrooms.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow

/**
 * Devuelve, de forma reactiva, las habitaciones libres para reserva.
 *
 * Alimenta el listado principal que ven los clientes en
 * [com.aojeda.jetpackstayrooms.ui.screen.RoomListScreen].
 */
class GetAvailableRoomsUseCase(private val roomRepository: RoomRepository) {
    operator fun invoke(): Flow<List<Room>> {
        return roomRepository.getAvailableRooms()
    }
}