package com.example.jetpackstayrooms.domain.repository

import com.example.jetpackstayrooms.domain.Room
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de acceso a habitaciones definido por el dominio.
 *
 * Permite que los casos de uso operen sobre habitaciones sin conocer Room ni
 * ningún otro detalle de persistencia.
 */
interface RoomRepository {

    /**
     * Persiste una nueva habitación.
     *
     * @return Identificador autogenerado de la habitación insertada.
     */
    suspend fun insertRoom(room: Room): Long

    /**
     * @return La habitación con el [roomId] dado, o `null` si no existe.
     */
    suspend fun getRoomById(roomId: Long): Room?

    /**
     * Flujo reactivo con todas las habitaciones del hostal, disponibles o no.
     */
    fun getAllRooms(): Flow<List<Room>>

    /**
     * Flujo reactivo con las habitaciones marcadas como disponibles
     * (`isAvailable = true`). Es el flujo que ven los clientes en la pantalla
     * principal de listado.
     */
    fun getAvailableRooms(): Flow<List<Room>>

    /**
     * Sobrescribe todos los campos de una habitación existente.
     */
    suspend fun updateRoom(room: Room)

    /**
     * Actualiza únicamente la disponibilidad de una habitación, sin tocar el
     * resto de campos.
     *
     * Lo usan los casos de uso de reserva/cancelación/finalización para liberar
     * u ocupar la habitación.
     */
    suspend fun updateRoomAvailability(roomId: Long, isAvailable: Boolean)
}